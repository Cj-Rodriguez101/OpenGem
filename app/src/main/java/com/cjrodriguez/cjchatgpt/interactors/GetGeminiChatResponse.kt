package com.cjrodriguez.cjchatgpt.interactors

import android.content.Context
import android.util.Log
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.SummaryEntity
import com.cjrodriguez.cjchatgpt.data.datasource.network.gemini.GeminiModelApi
import com.cjrodriguez.cjchatgpt.data.util.ERROR
import com.cjrodriguez.cjchatgpt.data.util.LOADING
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_HISTORY_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.data.util.getNewSummaryResponseFromModel
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendResponse
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendTopic
import com.cjrodriguez.cjchatgpt.data.util.storeImageInCache
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import com.cjrodriguez.cjchatgpt.presentation.util.shouldTriggerImageModel
import com.cjrodriguez.cjchatgpt.presentation.util.tryCatch
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
    private val chatTopicDao: ChatTopicDao,
) {
    fun execute(
        message: String,
        isNewChat: Boolean,
        isCurrentlyConnectedToInternet: Boolean,
        topicId: String
    ): Flow<DataState<String>> = flow {
        emit(DataState.loading())
        var errorMessage = ""
        var requestMessageId = ""

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
            val textGeminiModel = geminiModelApi.getGenerativeModel()
            val textResponseFlow = when{
                shouldGenerateImage -> null
                isNewChat -> textGeminiModel.generateContentStream(message)
                else -> {
                    val summaryEntity = chatTopicDao.getSummaryItemBasedOnTopic(topicId)
                    summaryEntity?.let {
                        val history = chatTopicDao.getAllChatsFromTopicNoPaging(topicId)
                        val contentList: MutableList<Content> = mutableListOf()
                        history.map {
                            if(it.imageUrl != "" ){
                                contentList.add(content(role = "user") {
                                    text(it.expandedContent)
                                })
                                contentList.add(content(role = "model") {
                                    text("The image created was found here ${it.imageUrl}")
                                })
                            } else {
                                contentList.add(
                                    content(role = if(it.isUserGenerated) "user" else "model") {
                                        text(it.expandedContent)
                                    }
                                )
                            }
                        }
                        val chat = textGeminiModel.startChat(
                            history = contentList
                        )
                        chat.sendMessageStream(message)
                    }?: textGeminiModel.generateContentStream(message)
                }
            }

            requestMessageId = generateRandomId()
            val responseMessageId = generateRandomId()
            val lastCreatedIndex = chatTopicDao.getMaxTimeCreatedAtWithTopic(topicId) ?: 0

            chatTopicDao.insertChatResponse(
                ChatEntity(
                    messageId = requestMessageId,
                    topicId = topicId,
                    expandedContent = message,
                    isUserGenerated = !shouldGenerateImage,
                    imageUrl = if (shouldGenerateImage) LOADING else "",
                    lastCreatedIndex = lastCreatedIndex + 1,
                    modelId = "gemini-pro"
                )
            )

            coroutineScope {
                val messageJob = async {
                    ensureActive()
                    textResponseFlow?.collectLatest { chunk ->
                        chunk.text?.let {
                            storeAndAppendResponse(
                                responseMessageId,
                                it,
                                topicId,
                                lastCreatedIndex,
                                "gemini-pro",
                                chatTopicDao
                            )
                        }
                    }?: textGeminiModel.generateContent(message).let {imageText ->
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
                                imageUrl = image ?: ERROR
                            )
                        }
                    }
                }
                if (isNewChat) {
                    val topicFlow =
                        textGeminiModel.generateContentStream("$SUMMARIZE_PROMPT $message")
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
                    val summary = getNewSummaryResponseFromModel(
                        topicId,
                        0,
                        chatTopicDao
                    ) { getSummarizedGeminiResponseFlow(it) }
                    summary.text.let {
                        Log.e("gemini", "new $it")
                        chatTopicDao.insertSummaryResponse(
                            SummaryEntity(
                                topicId = topicId,
                                content = it.toString()
                            )
                        )
                    }
                } else {
                    errorMessage = tryCatch(messageJob.await())
                    val lastCreatedId = chatTopicDao.getMaxTimeCreatedAtWithTopic(topicId) ?: 0
                    val summary = getNewSummaryResponseFromModel(
                        topicId,
                        lastCreatedId,
                        chatTopicDao
                    ) { getSummarizedGeminiResponseFlow(it) }
                    summary.text.let {
                        Log.e("gemini", "not new $it")
                        chatTopicDao.appendTextToSummary(topicId, it.toString())
                    }
                }
            }
        } catch (ex: Exception) {
            errorMessage = ex.message.toString()
        }

        if (errorMessage.isEmpty()) {
            emit(DataState.data(data = topicId))
        } else {
            if(chatTopicDao.getSpecificChat(
                topicId = topicId,
                messageId = requestMessageId
            )?.imageUrl == LOADING){
                chatTopicDao.updateImageUrl(
                    messageId = requestMessageId,
                    imageUrl = ERROR
                )
            }

            if(errorMessage.contains("multiturn")){
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

    private suspend fun getSummarizedGeminiResponseFlow(
        messages: List<ChatEntity>,
    ): GenerateContentResponse {
        val history =
            SUMMARIZE_HISTORY_PROMPT + messages.joinToString(",") {
                it.expandedContent
            }

        return geminiModelApi.getGenerativeModel().generateContent(history)
    }
}