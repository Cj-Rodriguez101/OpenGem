package com.cjrodriguez.cjchatgpt.interactors

import android.content.Context
import android.graphics.Bitmap
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.network.gemini.GeminiModelApi
import com.cjrodriguez.cjchatgpt.data.util.ERROR
import com.cjrodriguez.cjchatgpt.data.util.LOADING
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.createBitmapFromContentUri
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendResponse
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendTopic
import com.cjrodriguez.cjchatgpt.data.util.storeImageInCache
import com.cjrodriguez.cjchatgpt.data.util.storeInTempFolderAndReturnUrl
import com.cjrodriguez.cjchatgpt.data.util.triggerHapticFeedback
import com.cjrodriguez.cjchatgpt.domain.model.MessageWrapper
import com.cjrodriguez.cjchatgpt.presentation.util.AiType.GEMINI
import com.cjrodriguez.cjchatgpt.presentation.util.AiType.GEMINI_VISION
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import com.cjrodriguez.cjchatgpt.presentation.util.removeImagine
import com.cjrodriguez.cjchatgpt.presentation.util.shouldTriggerImageModel
import com.cjrodriguez.cjchatgpt.presentation.util.tryCatch
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetGeminiChatResponse @Inject constructor(
    private val context: Context,
    private val geminiModelApi: GeminiModelApi,
    private val chatTopicDao: ChatTopicDao
) {
    fun execute(
        messageWrapper: MessageWrapper,
        isNewChat: Boolean,
        isCurrentlyConnectedToInternet: Boolean,
        topicId: String
    ): Flow<DataState<String>> = flow {
        emit(DataState.loading())
        var errorMessage = ""
        var requestMessageId = ""

        try {
            val message = messageWrapper.message
            if (!isCurrentlyConnectedToInternet) {
                errorMessage = context.getString(R.string.no_internet_available)
                emit(
                    DataState.data(
                        data = if (isNewChat) {
                            ""
                        } else topicId,
                        message = GenericMessageInfo
                            .Builder().id("GetChatResponse.Error")
                            .title(context.getString(R.string.error))
                            .description(errorMessage)
                            .uiComponentType(UIComponentType.Dialog)
                    )
                )
                return@flow
            }
            requestMessageId = generateRandomId()
            val responseMessageId = generateRandomId()
            val bitmapList: List<Bitmap> = messageWrapper.fileUris.mapNotNull {
                createBitmapFromContentUri(context, it)
            }
            val shouldGenerateImage = shouldTriggerImageModel(message)
            val geminiModel = geminiModelApi.getGenerativeModel(
                if (messageWrapper.fileUris.isNotEmpty()) "gemini-pro-vision" else GEMINI.modelName
            )
            val lastCreatedIndex = chatTopicDao.getMaxTimeCreatedAtWithTopic(topicId) ?: 0
            val fileUrls = bitmapList.mapNotNull { bitmap ->
                storeInTempFolderAndReturnUrl(
                    bitmap,
                    topicId,
                    context
                )
            }

            chatTopicDao.insertChatResponse(
                ChatEntity(
                    messageId = requestMessageId,
                    topicId = topicId,
                    expandedContent = message.removeImagine(),
                    isUserGenerated = !shouldGenerateImage,
                    imageUrls = when {
                        fileUrls.isNotEmpty() -> fileUrls.toList()
                        shouldGenerateImage -> listOf(LOADING)
                        else -> listOf()
                    },
                    lastCreatedIndex = lastCreatedIndex.inc(),
                    modelId = GEMINI.modelName
                )
            )
            val textResponseFlow = when {
                shouldGenerateImage -> null
                else -> {
                    val contentList: MutableList<Content> = loadHistoryToGemini(
                        topicId,
                        chatTopicDao
                    )
                    getGeminiResponseFlow(
                        history = contentList,
                        bitmapList = bitmapList,
                        messageWrapper = messageWrapper,
                        generativeModel = geminiModel
                    )
                }
            }

            errorMessage = collectGeminiResponse(
                chatTopicDao,
                context,
                textResponseFlow,
                responseMessageId,
                topicId,
                lastCreatedIndex,
                geminiModel,
                geminiModelApi.getGenerativeModel(GEMINI.modelName),
                message,
                requestMessageId,
                isNewChat,
                errorMessage
            )
        } catch (ex: Exception) {
            errorMessage = ex.message.toString()
        }

        if (errorMessage.isEmpty()) {
            emit(DataState.data(data = topicId))
        } else {
            if (chatTopicDao.getSpecificChat(
                    topicId = topicId,
                    messageId = requestMessageId
                )?.imageUrls == listOf(LOADING)
            ) {
                chatTopicDao.updateImageUrl(
                    messageId = requestMessageId,
                    imageUrl = listOf(ERROR)
                )
            }

            if (errorMessage.isNotEmpty()) {
                chatTopicDao.deleteMessageId(requestMessageId)
            }
            emit(
                DataState.error(
                    message = GenericMessageInfo
                        .Builder().id("GetGeminiChatResponse.Error")
                        .title(context.getString(R.string.error))
                        .description(errorMessage)
                        .uiComponentType(UIComponentType.Dialog)
                )
            )
        }

    }
}

suspend fun collectGeminiResponse(
    chatTopicDao: ChatTopicDao,
    context: Context,
    textResponseFlow: Any?,
    responseMessageId: String,
    topicId: String,
    lastCreatedIndex: Int,
    selectedGeminiModel: GenerativeModel,
    textOnlyGeminiModel: GenerativeModel,
    message: String,
    requestMessageId: String,
    isNewChat: Boolean,
    error: String
): String {
    var errorMessage = error
    coroutineScope {
        val messageJob = async {
            ensureActive()
            if ((textResponseFlow as? GenerateContentResponse) != null) {
                triggerHapticFeedback(context)
                storeAndAppendResponse(
                    responseMessageId,
                    (textResponseFlow).text.toString(),
                    topicId,
                    lastCreatedIndex,
                    GEMINI_VISION.modelName,
                    listOf(),
                    chatTopicDao
                )
                return@async
            }
            (textResponseFlow as? Flow<GenerateContentResponse>)?.collectLatest { chunk ->
                chunk.text?.let {
                    triggerHapticFeedback(context)
                    storeAndAppendResponse(
                        responseMessageId,
                        it,
                        topicId,
                        lastCreatedIndex,
                        GEMINI.modelName,
                        listOf(),
                        chatTopicDao
                    )
                }
            } ?: selectedGeminiModel.generateContent(message).let { imageText ->
                //gemini api does not yet picture output
                imageText.text?.let {
                    val image = storeImageInCache(
                        imageUrl = it,
                        isImageUrl = false,
                        topicId = topicId,
                        context = context
                    )
                    chatTopicDao.updateImageUrl(
                        messageId = requestMessageId,
                        imageUrl = listOf(image ?: ERROR)
                    )
                }
            }
        }
        if (isNewChat) {
            val topicFlow =
                textOnlyGeminiModel
                    .generateContentStream("$SUMMARIZE_PROMPT $message")
            val topicJob = async {
                ensureActive()
                topicFlow.collectLatest { chunk ->
                    chunk.text?.let {
                        storeAndAppendTopic(
                            topicId,
                            it,
                            chatTopicDao
                        )
                    }
                }
            }

            errorMessage = tryCatch(awaitAll(messageJob, topicJob))
        } else {
            errorMessage = tryCatch(messageJob.await())
        }
    }
    return errorMessage
}

fun loadHistoryToGemini(topicId: String, chatTopicDao: ChatTopicDao): MutableList<Content> {
    val history = chatTopicDao.getAllChatsFromTopicNoPaging(topicId)
    val index = history.indexOfLast { !it.isUserGenerated }
    val mutableHistory = if (index == -1) {
        listOf()
    } else {
        history.subList(0, index + 1)
    }
    val contentList: MutableList<Content> = mutableListOf()
    mutableHistory.map {
        if (it.imageUrls.isNotEmpty()) {
            contentList.add(content(role = "user") {
                text(it.expandedContent)
            })
            contentList.add(content(role = "model") {
                text("The image created was found here ${it.imageUrls}")
            })
        } else {
            contentList.add(
                content(role = if (it.isUserGenerated) "user" else "model") {
                    text(it.expandedContent)
                }
            )
        }
    }
    return contentList
}

suspend fun getGeminiResponseFlow(
    history: List<Content> = listOf(),
    bitmapList: List<Bitmap> = listOf(),
    messageWrapper: MessageWrapper,
    generativeModel: GenerativeModel
): Any {
    val contentToSend = content("user") {
        bitmapList.map { bitmap ->
            image(bitmap)
        }
        text(messageWrapper.message)
    }
    return if (messageWrapper.fileUris.isNotEmpty()) {
        generativeModel.generateContent(contentToSend)
    } else {
        val chat = generativeModel.startChat(
            history = history
        )
        chat.sendMessageStream(contentToSend)
    }
}