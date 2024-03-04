package com.cjrodriguez.cjchatgpt.data.repository.chat

import androidx.paging.PagingData
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.domain.model.MessageWrapper
import com.cjrodriguez.cjchatgpt.presentation.util.AiType
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {

    fun getAndStoreOpenAiChatResponse(
        message: MessageWrapper,
        isNewChat: Boolean,
        topicId: String,
        isCurrentlyConnectedToInternet: Boolean,
        model: String
    ): Flow<DataState<String>>

    fun getAndStoreGeminiResponse(
        message: MessageWrapper,
        isNewChat: Boolean,
        topicId: String,
        isCurrentlyConnectedToInternet: Boolean
    ): Flow<DataState<String>>

    fun getTextFromSpeech(): Flow<DataState<String>>

    fun copyTextToClipboard(textToCopy: String): Flow<DataState<Unit>>

    fun setGptVersion(aiType: AiType)

    fun getGptVersion(): Flow<String>

    fun getAllChats(topicId: String): Flow<PagingData<Chat>>

    fun getSelectedTopicName(topicId: String): Flow<String?>

    fun getPowerLevelWithListening(): StateFlow<Float>

    fun startRecording()

    fun setRecordingState(isRecordingState: Boolean)

    suspend fun updatePowerLevel()

    fun saveImage(imagePath: String): Flow<DataState<String>>

    fun stopRecording()
}