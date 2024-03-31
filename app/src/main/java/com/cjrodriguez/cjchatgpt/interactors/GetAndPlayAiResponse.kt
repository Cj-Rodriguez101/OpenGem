package com.cjrodriguez.cjchatgpt.interactors

import android.content.Context
import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.audio.Voice
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.network.gemini.GeminiModelApi
import com.cjrodriguez.cjchatgpt.data.datasource.network.open_ai.OpenApiConfig
import com.cjrodriguez.cjchatgpt.data.util.ERROR
import com.cjrodriguez.cjchatgpt.data.util.LOADING
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.domain.model.MessageWrapper
import com.cjrodriguez.cjchatgpt.presentation.util.AiType.GEMINI
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import com.google.ai.client.generativeai.type.Content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class GetAndPlayAiResponse @Inject constructor(
    private val context: Context,
    private val openApiConfig: OpenApiConfig,
    private val geminiModelApi: GeminiModelApi,
    private val chatTopicDao: ChatTopicDao
) {
    fun execute(
        isNewChat: Boolean,
        isCurrentlyConnectedToInternet: Boolean,
        topicId: String,
        model: String,
        isOpenAi: Boolean
    ): Flow<DataState<SoundAndTopicId>> = flow {
        emit(DataState.loading())
        var errorMessage = ""
        var requestMessageId = ""
        try {
            if (!isCurrentlyConnectedToInternet) {
                errorMessage = context.getString(R.string.no_internet_available)
                emit(
                    DataState.data(
                        data = SoundAndTopicId(topicId),
                        message = GenericMessageInfo
                            .Builder().id("GetAndPlayAiResponse.Error")
                            .title(context.getString(R.string.error))
                            .description(errorMessage)
                            .uiComponentType(UIComponentType.Dialog)
                    )
                )
                return@flow
            }
            requestMessageId = generateRandomId()
            val responseMessageId = generateRandomId()
            val lastCreatedIndex = chatTopicDao.getMaxTimeCreatedAtWithTopic(topicId) ?: 0
            val fileToDeleted = File(context.cacheDir, "tmp.mp3")
            val source = fileToDeleted.getRecordingSource()
            val request = TranscriptionRequest(
                audio = FileSource(name = fileToDeleted.name, source = source),
                model = ModelId("whisper-1"),
            )
            val transcription = openApiConfig.openai.transcription(request)
            fileToDeleted.delete()
            val voiceInputMessage = transcription.text
            chatTopicDao.insertChatResponse(
                ChatEntity(
                    messageId = requestMessageId,
                    topicId = topicId,
                    expandedContent = voiceInputMessage,
                    modelId = model,
                    lastCreatedIndex = lastCreatedIndex.inc()
                )
            )
            val messageWrapper =
                MessageWrapper(message = "\"$voiceInputMessage\", respond directly and conclude by asking if there's another request.")

            if (isOpenAi) {
                val contentList: MutableList<ChatMessage> =
                    loadHistoryToOpenAi(topicId, chatTopicDao)
                val textResponseFlow =
                    getOpenAiResponseFlow(
                        openApiConfig = openApiConfig,
                        history = contentList,
                        messageWrapper = messageWrapper,
                        model = model
                    )
                errorMessage = collectOpenAiResponse(
                    chatTopicDao,
                    context,
                    openApiConfig,
                    textResponseFlow,
                    responseMessageId,
                    topicId,
                    lastCreatedIndex,
                    model,
                    model,
                    voiceInputMessage,
                    requestMessageId,
                    isNewChat,
                    errorMessage
                )

            } else {
                val geminiModel = geminiModelApi.getGenerativeModel(GEMINI.modelName)
                val textResponseFlow = when {
                    isNewChat -> getGeminiResponseFlow(
                        messageWrapper = messageWrapper,
                        generativeModel = geminiModel
                    )

                    else -> {
                        val contentList: MutableList<Content> =
                            loadHistoryToGemini(topicId, chatTopicDao)
                        getGeminiResponseFlow(
                            history = contentList,
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
                    geminiModel,
                    voiceInputMessage,
                    requestMessageId,
                    isNewChat,
                    errorMessage
                )
            }
            chatTopicDao.getSpecificChat(
                topicId = topicId,
                messageId = responseMessageId
            )?.let {
                val rawAudio = openApiConfig.openai.speech(
                    request = SpeechRequest(
                        model = ModelId("tts-1"),
                        input = it.expandedContent,
                        voice = Voice.Alloy,
                    )
                )
                emit(
                    DataState.data(
                        data = SoundAndTopicId(
                            topicId, createTempFileFromByteArray(rawAudio, context).absolutePath
                        )
                    )
                )
            }

        } catch (ex: Exception) {
            errorMessage = ex.message.toString()
        }

        if (errorMessage.isEmpty()) {
            emit(DataState.data(data = SoundAndTopicId(topicId)))
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
}

fun createTempFileFromByteArray(
    byteArray: ByteArray,
    context: Context
): File {
    val tempFile = File.createTempFile(
        "tempAudio",
        "mp3",
        context.cacheDir
    ).apply {
        deleteOnExit()
    }

    FileOutputStream(tempFile).use { output ->
        output.write(byteArray)
    }

    return tempFile
}

data class SoundAndTopicId(
    val topicId: String,
    val audioPath: String = ""
)