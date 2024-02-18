package com.cjrodriguez.cjchatgpt.interactors

import android.content.Context
import android.util.Log
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.SummaryEntity
import com.cjrodriguez.cjchatgpt.data.datasource.network.gemini.GeminiModelApi
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
import com.google.ai.client.generativeai.type.GenerateContentResponse
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
        topicId: String,
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
                geminiModelApi.generativeModel.generateContentStream(message)
            } else {
                val summaryEntity = chatTopicDao.getSummaryItemBasedOnTopic(topicId)
                if (summaryEntity != null) {
                    geminiModelApi.generativeModel.generateContentStream(
                        "$CHAT_HISTORY_REFER_PROMPT \"${summaryEntity.content}\" " +
                                "$THE_REAL_PROMPT_IS  \"$message\"",
                    )
                } else {
                    geminiModelApi.generativeModel.generateContentStream(message)
                }
            }

            val messageId = generateRandomId()
            val lastCreatedIndex = chatTopicDao.getMaxTimeCreatedAtWithTopic(topicId) ?: 0

            chatTopicDao.insertChatResponse(
                ChatEntity(
                    messageId = generateRandomId(),
                    topicId = topicId,
                    expandedContent = message,
                    lastCreatedIndex = lastCreatedIndex + 1
                )
            )

            coroutineScope {
                val messageJob = async {
                    ensureActive()
                    responseFlow.collectLatest { chunk ->
                        chunk.text?.let {
                            storeAndAppendResponse(
                                messageId,
                                it,
                                topicId,
                                lastCreatedIndex,
                                "gemini-pro",
                                chatTopicDao
                            )
                        }
                    }
                }
                if (isNewChat) {
                    val topicFlow =
                        geminiModelApi.generativeModel.generateContentStream("$SUMMARIZE_PROMPT $message")
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

        return geminiModelApi.generativeModel.generateContent(history)
    }
}