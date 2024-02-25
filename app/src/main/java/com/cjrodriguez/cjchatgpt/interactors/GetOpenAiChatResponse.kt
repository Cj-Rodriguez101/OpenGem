package com.cjrodriguez.cjchatgpt.interactors

import android.content.Context
import android.util.Log
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.api.model.ModelId
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.SummaryEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.getChatRole
import com.cjrodriguez.cjchatgpt.data.datasource.network.open_ai.OpenApiConfig
import com.cjrodriguez.cjchatgpt.data.util.CHAT_HISTORY_REFER_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.ERROR
import com.cjrodriguez.cjchatgpt.data.util.LOADING
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_HISTORY_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.SUMMARIZE_PROMPT
import com.cjrodriguez.cjchatgpt.data.util.THE_REAL_PROMPT_IS
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
            val textResponseFlow = when {
                shouldGenerateImage -> null
                isNewChat -> getOpenAiResponseFlow(message, model)
                else -> chatTopicDao.getSummaryItemBasedOnTopic(topicId)?.let { summaryEntity ->
                    getOpenAiResponseFlow(
                        "$CHAT_HISTORY_REFER_PROMPT \"${summaryEntity.content}\" " +
                                "$THE_REAL_PROMPT_IS  \"$message\"",
                                                //"""
//                    Given the context provided, please focus on addressing the current query:
//                    Context: ${summaryEntity.content}
//                    Current Query: $message
//                    Please generate a response that is directly relevant to the current query,
//                     utilizing the context as needed to inform the response accurately.
//                      If the context is not necessary to answer the current query,
//                       please proceed without referencing it and think about it yourself.
//                        Never say anything like the provided context does not contain information about whatever.
//                    """,
                        model
                    )
                } ?: getOpenAiResponseFlow(message, model)
            }

            requestMessageId = generateRandomId()
            val responseMessageId = generateRandomId()
            val lastCreatedIndex = chatTopicDao.getMaxTimeCreatedAtWithTopic(topicId) ?: 0

            chatTopicDao.insertChatResponse(
                ChatEntity(
                    messageId = requestMessageId,
                    topicId = topicId,
                    expandedContent = message,
                    modelId = if (shouldGenerateImage) "dall-e-3" else model,
                    isUserGenerated = !shouldGenerateImage,
                    imageUrl = if (shouldGenerateImage) LOADING else "",
                    lastCreatedIndex = lastCreatedIndex + 1
                )
            )

            coroutineScope {
                val messageJob = async {
                    ensureActive()
                    textResponseFlow?.collectLatest { chunk ->
                        chunk.choices[0].delta.content?.let {
                            storeAndAppendResponse(
                                responseMessageId,
                                it,
                                topicId,
                                lastCreatedIndex,
                                model,
                                chatTopicDao
                            )
                        }
                    }?: getImageFromOpenAiPrompt(message).let {imageUrls ->
                        if (imageUrls.isNotEmpty()){
                            val image = storeImageInCache(
                                imageUrl = imageUrls[0].url,
                                context = context,
                                topicId = topicId
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
                        getOpenAiResponseFlow(
                            "$SUMMARIZE_PROMPT $message",
                            "gpt-3.5-turbo")
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
                        chatTopicDao.appendTextToSummary(topicId, it.toString())
                    }
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
            if(chatTopicDao.getSpecificChat(
                    topicId = topicId,
                    messageId = requestMessageId
                )?.imageUrl == LOADING){
                chatTopicDao.updateImageUrl(
                    messageId = requestMessageId,
                    imageUrl = ERROR
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