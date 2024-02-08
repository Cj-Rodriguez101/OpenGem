package com.cjrodriguez.cjchatgpt.interactors

import android.content.Context
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity
import com.cjrodriguez.cjchatgpt.data.datasource.network.open_ai.OpenApiConfig
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendResponse
import com.cjrodriguez.cjchatgpt.data.util.storeAndAppendTopic
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

    @OptIn(BetaOpenAI::class)
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
            val responseFlow = getOpenAiResponseFlow(message, model)
            val topicFlow =
                getOpenAiResponseFlow("$SUMMARIZE_PROMPT $message", model)

            val messageId = generateRandomId()

            val lastCreatedIndex = chatTopicDao.getMaxTimeCreatedAt() ?: 0

            chatTopicDao.insertChatResponse(
                ChatEntity(
                    messageId = generateRandomId(),
                    topicId = topicId,
                    expandedContent = message,
                    lastCreatedIndex = lastCreatedIndex + 1
                )
            )

            coroutineScope {
                val job1 = async {
                    ensureActive()
                    responseFlow.collectLatest { chunk ->
                        chunk.choices[0].delta?.content?.let {
                            storeAndAppendResponse(
                                messageId,
                                it,
                                topicId,
                                lastCreatedIndex,
                                chatTopicDao
                            )
                        }
                    }
                }
                if (isNewChat) {
                    val job2 = async {
                        ensureActive()
                        topicFlow.collectLatest { chunk ->
                            chunk.choices[0].delta?.content?.let {
                                storeAndAppendTopic(
                                    topicId,
                                    it,
                                    chatTopicDao
                                )
                            }
                        }
                    }

                    errorMessage = tryCatch(awaitAll(job1, job2))

                } else {
                    errorMessage = tryCatch(job1.await())
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
                        .Builder().id("GetoOpenAiChatResponse.Error")
                        .title(context.getString(R.string.error))
                        .description(errorMessage)
                        .uiComponentType(UIComponentType.Dialog)
                )
            )
        }

    }

    @OptIn(BetaOpenAI::class)
    private fun getOpenAiResponseFlow(
        message: String,
        model: String
    ) = openApiConfig.openai.chatCompletions(
        ChatCompletionRequest(
            messages = listOf(ChatMessage(role = ChatRole.User, content = message)),
            model = ModelId(model)
        )
    )
}