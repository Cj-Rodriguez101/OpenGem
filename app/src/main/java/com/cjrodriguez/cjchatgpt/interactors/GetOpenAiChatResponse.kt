package com.cjrodriguez.cjchatgpt.interactors

import android.content.Context
import android.util.Log
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.SummaryEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.getChatRole
import com.cjrodriguez.cjchatgpt.data.datasource.network.open_ai.OpenApiConfig
import com.cjrodriguez.cjchatgpt.data.util.CHAT_HISTORY_REFER_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_HISTORY_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.THE_REAL_PROMPT_IS
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.data.util.getNewSummaryResponseFromModel
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendResponse
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendTopic
import com.cjrodriguez.cjchatgpt.data.util.toByteArrayCustom
import com.cjrodriguez.cjchatgpt.data.util.toCustomString
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import com.cjrodriguez.cjchatgpt.presentation.util.tryCatch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetOpenAiChatResponse @Inject constructor(
    private val context: Context,
    private val openApiConfig: OpenApiConfig,
    private val chatTopicDao: ChatTopicDao,
) {
    fun execute(
        message: String,
        isNewChat: Boolean,
        isCurrentlyConnectedToInternet: Boolean,
        topicId: String,
        model: String
    ): Flow<DataState<String>> = flow {

        emit(DataState.loading())
        var errorMessage = ""

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
            val responseFlow = if (isNewChat) {
                getOpenAiResponseFlow(message, model)
            } else {
                //get summary text
                val summaryEntity = chatTopicDao.getSummaryItemBasedOnTopic(topicId)
                if (summaryEntity != null) {
                    getOpenAiResponseFlow(
                        "$CHAT_HISTORY_REFER_PROMPT \"${summaryEntity.content}\" " +
                                "$THE_REAL_PROMPT_IS  \"$message\"",
                        model
                    )
                } else {
                    getOpenAiResponseFlow(message, model)
                }
            }

            val messageId = generateRandomId()
            val lastCreatedIndex = chatTopicDao.getMaxTimeCreatedAtWithTopic(topicId) ?: 0

            chatTopicDao.insertChatResponse(
                ChatEntity(
                    messageId = generateRandomId(),
                    topicId = topicId,
                    expandedContent = message,
                    modelId = model,
                    lastCreatedIndex = lastCreatedIndex + 1
                )
            )

            coroutineScope {
                val messageJob = async {
                    ensureActive()
                    responseFlow.collectLatest { chunk ->
                        chunk.choices[0].delta.content?.let {
                            storeAndAppendResponse(
                                messageId,
                                it,
                                topicId,
                                lastCreatedIndex,
                                model,
                                chatTopicDao
                            )
                        }
                    }
                }
                if (isNewChat) {
                    val topicFlow =
                        getOpenAiResponseFlow("$SUMMARIZE_PROMPT $message", "gpt-3.5-turbo")
                    val topicJob = async {
                        ensureActive()
                        topicFlow.collectLatest { chunk ->
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
                    val summary = getNewSummaryResponseFromModel(
                        topicId,
                        0,
                        chatTopicDao
                    ) { getSummarizedOpenAiResponseFlow(it) }
                    summary.choices[0].message.content.let {
                        Log.e("openai", "new $it")
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
                    ) { getSummarizedOpenAiResponseFlow(it) }
                    summary.choices[0].message.content.let {
                        Log.e("openai", "not new $it")
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

    private fun getOpenAiResponseFlow(
        message: String,
        model: String
    ) = openApiConfig.openai.chatCompletions(
        ChatCompletionRequest(
            messages = listOf(ChatMessage(role = ChatRole.User, content = message)),
            model = ModelId(model)
        )
    )

    private suspend fun getSummarizedOpenAiResponseFlow(
        messages: List<ChatEntity>,
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

//    private suspend fun getContent(): List<ImageURL>{
//        return openApiConfig.openai.imageURL(
//            ImageCreation(prompt = "", model = ModelId(""), n=2, size = ImageSize.is1024x1024))
//    }
}