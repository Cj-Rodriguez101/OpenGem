package com.cjrodriguez.cjchatgpt.interactors

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ContentPart
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.api.model.ModelId
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.getChatRole
import com.cjrodriguez.cjchatgpt.data.datasource.network.open_ai.OpenApiConfig
import com.cjrodriguez.cjchatgpt.data.util.ERROR
import com.cjrodriguez.cjchatgpt.data.util.LOADING
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_HISTORY_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.createBitmapFromContentUri
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendResponse
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendTopic
import com.cjrodriguez.cjchatgpt.data.util.storeByteArrayToTemp
import com.cjrodriguez.cjchatgpt.data.util.storeImageInCache
import com.cjrodriguez.cjchatgpt.data.util.toCustomString
import com.cjrodriguez.cjchatgpt.domain.model.MessageWrapper
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import com.cjrodriguez.cjchatgpt.presentation.util.shouldTriggerImageModel
import com.cjrodriguez.cjchatgpt.presentation.util.tryCatch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import org.apache.commons.io.output.ByteArrayOutputStream
import javax.inject.Inject

class GetOpenAiChatResponse @Inject constructor(
    private val context: Context,
    private val openApiConfig: OpenApiConfig,
    private val chatTopicDao: ChatTopicDao
) {
    fun execute(
        messageWrapper: MessageWrapper,
        isNewChat: Boolean,
        isCurrentlyConnectedToInternet: Boolean,
        topicId: String,
        model: String
    ): Flow<DataState<String>> = flow {
        val message = messageWrapper.message
        emit(DataState.loading())
        var errorMessage = ""
        var requestMessageId = ""
        val modifiedModel =
            if (messageWrapper.fileUris.isNotEmpty()) "gpt-4-vision-preview" else model

        try {
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
            val shouldGenerateImage = shouldTriggerImageModel(message)
            val textResponseFlow = when {
                shouldGenerateImage -> null
                isNewChat -> getOpenAiResponseFlow(
                    messageWrapper = messageWrapper,
                    model = modifiedModel
                )

                else -> {
                    val history = chatTopicDao.getAllChatsFromTopicNoPaging(topicId)
                    val contentList: MutableList<ChatMessage> = mutableListOf()
                    history.map {
                        if (it.imageUrls.isNotEmpty()) {
                            contentList.add(
                                ChatMessage(
                                    role = ChatRole.User,
                                    content = it.expandedContent
                                )
                            )
                            contentList.add(
                                ChatMessage(
                                    role = ChatRole.System,
                                    content = "The image created was found here ${it.imageUrls}"
                                )
                            )
                        } else {
                            contentList.add(
                                ChatMessage(
                                    role = if (it.isUserGenerated) ChatRole.User else ChatRole.System,
                                    content = it.expandedContent
                                )
                            )
                        }
                    }
                    getOpenAiResponseFlow(contentList, messageWrapper, modifiedModel)
//                    chatTopicDao.getSummaryItemBasedOnTopic(topicId)?.let { summaryEntity ->
//                        getOpenAiResponseFlow(
//                            "$CHAT_HISTORY_REFER_PROMPT \"${(summaryEntity.content).toCustomString()}\" " +
//                                    "$THE_REAL_PROMPT_IS  \"$message\"",
//                            model
//                        )
//                    } ?: getOpenAiResponseFlow(message, model)
                }
            }

            requestMessageId = generateRandomId()
            val responseMessageId = generateRandomId()
            val lastCreatedIndex = chatTopicDao.getMaxTimeCreatedAtWithTopic(topicId) ?: 0
            val fileUrls: MutableList<String> = mutableListOf()
            if (messageWrapper.fileUris.isNotEmpty()) {
                messageWrapper.fileUris.map {
                    createBitmapFromContentUri(context, it)?.let { bitmap ->
                        val stream = java.io.ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        val byteArray = stream.toByteArray() ?: return@let
                        val url = storeByteArrayToTemp(
                            topicId = topicId,
                            context = context,
                            imageTitle = byteArray.toCustomString(),
                            byteArray = byteArray
                        ) ?: return@let
                        fileUrls.add(url)
                    }
                }
            }

            chatTopicDao.insertChatResponse(
                ChatEntity(
                    messageId = requestMessageId,
                    topicId = topicId,
                    expandedContent = message,
                    modelId = if (shouldGenerateImage) "dall-e-3" else model,
                    isUserGenerated = !shouldGenerateImage,
                    imageUrls = when {
                        fileUrls.isNotEmpty() -> fileUrls.toList()
                        shouldGenerateImage -> listOf(LOADING)
                        else -> listOf()
                    },
                    lastCreatedIndex = lastCreatedIndex + 1
                )
            )

            coroutineScope {
                val messageJob = async {
                    ensureActive()
                    if ((textResponseFlow as? ChatCompletion) != null) {
                        storeAndAppendResponse(
                            messageId = responseMessageId,
                            (textResponseFlow).choices[0].message.content.toString(),
                            topicId,
                            lastCreatedIndex,
                            modifiedModel,
                            listOf(),
                            chatTopicDao
                        )
                        return@async
                    }
                    (textResponseFlow as? Flow<ChatCompletionChunk>)?.collectLatest { chunk ->
                        chunk.choices[0].delta.content?.let {
                            storeAndAppendResponse(
                                messageId = responseMessageId,
                                it,
                                topicId,
                                lastCreatedIndex,
                                model,
                                listOf(),
                                chatTopicDao
                            )
                        }
                    } ?: getImageFromOpenAiPrompt(message).let { imageUrls ->
                        if (imageUrls.isNotEmpty()) {
                            val images = imageUrls.map {
                                storeImageInCache(
                                    imageUrl = it.url,
                                    context = context,
                                    topicId = topicId
                                ) ?: ERROR
                            }
                            chatTopicDao.updateImageUrl(
                                messageId = requestMessageId,
                                imageUrl = images
                            )
//                            val image = storeImageInCache(
//                                imageUrl = imageUrls[0].url,
//                                context = context,
//                                topicId = topicId
//                            )
//                            chatTopicDao.updateImageUrl(
//                                messageId = requestMessageId,
//                                imageUrl = image ?: ERROR
//                            )
                        }
                    }
                }

                if (isNewChat) {
                    val topicFlow =
                        getOpenAiResponseFlow(
                            listOf(),
                            MessageWrapper(
                                message = "$SUMMARIZE_PROMPT $message",
                                fileUris = listOf()
                            ),
                            "gpt-3.5-turbo"
                        )
                    val topicJob = async {
                        ensureActive()
                        (topicFlow as? Flow<ChatCompletionChunk>)?.collectLatest { chunk ->
                            chunk.choices[0].delta.content?.let {
                                storeAndAppendTopic(
                                    topicId,
                                    it,
                                    chatTopicDao
                                )
                            }
                        }
                    }

                    errorMessage = tryCatch(awaitAll(messageJob, topicJob))
//                    val summary = getNewSummaryResponseFromModel(
//                        topicId,
//                        0,
//                        chatTopicDao
//                    ) { getSummarizedOpenAiResponseFlow(it) }
//                    summary.choices[0].message.content?.let {
//                        Log.e("openai", "new summary \n $it")
//                        chatTopicDao.insertSummaryResponse(
//                            SummaryEntity(
//                                topicId = topicId,
//                                content = it.toByteArrayCustom()
//                            )
//                        )
//                    }
                } else {
                    errorMessage = tryCatch(messageJob.await())
//                    val lastCreatedId = chatTopicDao.getMaxTimeCreatedAtWithTopic(topicId) ?: 0
//                    val summary = getNewSummaryResponseFromModel(
//                        topicId,
//                        lastCreatedId,
//                        chatTopicDao
//                    ) { if (!shouldGenerateImage) getSummarizedOpenAiResponseFlow(it) else null }
//                    summary?.choices?.get(0)?.message?.content.let {
//                        val summaryInCache =
//                            chatTopicDao.getSummaryItemBasedOnTopic(topicId) ?: return@let
//                        val decryptedString =
//                            (summaryInCache.content).toCustomString() + " ${it ?: "An image created using this prompt $message"}"
//                        chatTopicDao.insertSummaryResponse(
//                            summaryInCache
//                                .copy(
//                                    content = decryptedString.toByteArrayCustom()
//                                )
//                        )
//                    }
                }
//                if (shouldGenerateImage) chatTopicDao
//                    .updateImageDescription(messageId, message)
            }

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
            emit(
                DataState.error(
                    message = GenericMessageInfo
                        .Builder().id("GetOpenAiChatResponse.Error")
                        .title(context.getString(R.string.error))
                        .description(errorMessage)
                        .uiComponentType(UIComponentType.Dialog)
                )
            )
        }
    }

    private suspend fun getOpenAiResponseFlow(
        history: List<ChatMessage> = listOf(),
        messageWrapper: MessageWrapper,
        model: String
    ): Any {
        val mutableHistory = history.toMutableList()
        mutableHistory.add(
            ChatMessage(
                role = ChatRole.User,
                content = mutableListOf<ContentPart>().apply {
                    if (messageWrapper.fileUris.isNotEmpty()) {
                        messageWrapper.fileUris.map {
                            createBitmapFromContentUri(context, it)?.let { bitmap ->
                                ByteArrayOutputStream()
                                    .use { outputStream ->
                                        bitmap.compress(
                                            Bitmap.CompressFormat.PNG,
                                            100,
                                            outputStream
                                        )
                                        outputStream.toByteArray()?.let {
                                            Base64.encodeToString(it, Base64.DEFAULT)
                                                ?.let { encodedString ->
                                                    add(
                                                        ImagePart(
                                                            url = "data:image/jpeg;base64,$encodedString"
                                                        )
                                                    )
                                                }
                                        }
                                    }
                            }
                        }
                    }
                    add(TextPart(messageWrapper.message))
                }.toList()
            )
        )
        return if (messageWrapper.fileUris.isNotEmpty()) {
            openApiConfig.openai.chatCompletion(
                ChatCompletionRequest(
                    messages = mutableHistory.toList(),
                    model = ModelId(model)
                )
            )
        } else {
            openApiConfig.openai.chatCompletions(
                ChatCompletionRequest(
                    messages = mutableHistory.toList(),
                    model = ModelId(model)
                )
            )
        }
    }

    private suspend fun getSummarizedOpenAiResponseFlow(
        messages: List<ChatEntity>
    ) = openApiConfig.openai.chatCompletion(
        ChatCompletionRequest(
            messages = messages.mapIndexed { index, chatEntity ->
                if (index == 0) {
                    ChatMessage(
                        role = chatEntity.openAiChatRole.getChatRole(),
                        SUMMARIZE_HISTORY_PROMPT + chatEntity.expandedContent
                    )
                } else {
                    ChatMessage(
                        role = chatEntity.openAiChatRole.getChatRole(),
                        chatEntity.expandedContent
                    )
                }
            },
            model = ModelId("gpt-3.5-turbo")
        )
    )

    private suspend fun getImageFromOpenAiPrompt(prompt: String): List<ImageURL> {
        return openApiConfig.openai.imageURL(
            ImageCreation(
                prompt = prompt,
                model = ModelId("dall-e-3"),
                n = 1,
                size = ImageSize.is1024x1024
            )
        )
    }
}