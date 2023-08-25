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
import com.cjrodriguez.cjchatgpt.data.datasource.network.OpenApiConfig
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetChatResponse @Inject constructor(
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
                emit(DataState.data(data = if (isNewChat) {""} else topicId,
                    message = GenericMessageInfo
                        .Builder().id("GetChatResponse.Error")
                        .title(context.getString(R.string.error))
                        .description(errorMessage)
                        .uiComponentType(UIComponentType.Dialog)))
                return@flow
            }
            val chatCompletionNew = getOpenAiResponseFlow(message, model)
            val chatCompletionTopic =
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
                    chatCompletionNew.collectLatest { chunk ->
                        chunk.choices[0].delta?.content?.let {
                            val affectedRows =
                                chatTopicDao.appendTextToContentMessage(messageId, it)

                            if (affectedRows == 0) {
                                chatTopicDao.insertChatResponse(
                                    ChatEntity(
                                        messageId = messageId,
                                        topicId = topicId,
                                        expandedContent = it,
                                        isUserGenerated = false,
                                        lastCreatedIndex = lastCreatedIndex + 2
                                    )
                                )
                            }
                        }

                    }
                }
                if (isNewChat) {
                    val job2 = async {
                        ensureActive()
                        chatCompletionTopic.collectLatest { chunk ->
                            chunk.choices[0].delta?.content?.let {
                                val affectedRows = chatTopicDao.appendTextToTopicTitle(
                                    topicId,
                                    it
                                )

                                if (affectedRows == 0) {
                                    chatTopicDao.insertTopic(
                                        TopicEntity(
                                            id = topicId, title = it
                                        )
                                    )
                                }
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
                        .Builder().id("GetChatResponse.Error")
                        .title(context.getString(R.string.error))
                        .description(errorMessage)
                        .uiComponentType(UIComponentType.Dialog)
                )
            )
        }


    }

    private fun <T> tryCatch(input: T): String {
        var errorMessage = ""
        try {
            input
        } catch (ex: Exception) {
            errorMessage = ex.message.toString()
        }
        return errorMessage
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