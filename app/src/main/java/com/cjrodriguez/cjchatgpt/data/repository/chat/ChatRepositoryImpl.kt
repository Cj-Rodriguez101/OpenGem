package com.cjrodriguez.cjchatgpt.data.repository.chat

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cjrodriguez.cjchatgpt.data.datasource.audio.Recorder
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.dataStore.SettingsDataStore
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.domain.model.MessageWrapper
import com.cjrodriguez.cjchatgpt.interactors.CopyTextToClipBoard
import com.cjrodriguez.cjchatgpt.interactors.GetGeminiChatResponse
import com.cjrodriguez.cjchatgpt.interactors.GetOpenAiChatResponse
import com.cjrodriguez.cjchatgpt.interactors.GetTextFromSpeech
import com.cjrodriguez.cjchatgpt.interactors.SaveGeneratedImage
import com.cjrodriguez.cjchatgpt.presentation.util.AiType
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.toChat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val getOpenAiChatResponse: GetOpenAiChatResponse,
    private val getGeminiChatResponse: GetGeminiChatResponse,
    private val getTextFromSpeech: GetTextFromSpeech,
    private val copyTextToClipBoard: CopyTextToClipBoard,
    private val saveGeneratedImage: SaveGeneratedImage,
    private val dao: ChatTopicDao,
    private val settingsDataStore: SettingsDataStore,
    private val recorder: Recorder,
) : ChatRepository {
    override fun getAndStoreOpenAiChatResponse(
        message: MessageWrapper,
        isNewChat: Boolean,
        topicId: String,
        isCurrentlyConnectedToInternet: Boolean,
        model: String
    ): Flow<DataState<String>> {
        return getOpenAiChatResponse.execute(
            message,
            isNewChat,
            isCurrentlyConnectedToInternet,
            topicId,
            model
        )
    }

    override fun getAndStoreGeminiResponse(
        message: MessageWrapper,
        isNewChat: Boolean,
        topicId: String,
        isCurrentlyConnectedToInternet: Boolean
    ): Flow<DataState<String>> {
        return getGeminiChatResponse.execute(
            message,
            isNewChat,
            isCurrentlyConnectedToInternet,
            topicId
        )
    }

    override fun copyTextToClipboard(textToCopy: String): Flow<DataState<Unit>> {
        return copyTextToClipBoard.execute(textToCopy)
    }

    override fun getTextFromSpeech(): Flow<DataState<String>> {
        return getTextFromSpeech.execute()
    }

    override fun setGptVersion(aiType: AiType) {
        settingsDataStore.writeGptVersion(aiType.name)
    }

    override fun getGptVersion(): Flow<String> {
        return settingsDataStore.gptVersionFlow
    }

    override fun getAllChats(topicId: String): Flow<PagingData<Chat>> {
        return Pager(config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            maxSize = 20 * 3
        ),
            pagingSourceFactory = { dao.getAllChatsFromTopic(topicId) }).flow
            .map { pagingData ->
                pagingData.map { it.toChat() }
            }
    }

    override fun getSelectedTopicName(topicId: String): Flow<String?> {
        return dao.getSpecificTopic(topicId)
    }

    override fun getPowerLevelWithListening(): StateFlow<Float> {
        return recorder.getPowerLevel()
    }

    override fun startRecording() {
        recorder.startRecording()
    }

    override fun setRecordingState(isRecordingState: Boolean) {
        recorder.setRecordingState(isRecordingState)
    }

    override suspend fun updatePowerLevel() {
        withContext(Dispatchers.IO) {
            recorder.updatePowerLevel()
        }
    }

    override fun saveImage(imagePath: String): Flow<DataState<String>> {
        return saveGeneratedImage.execute(imagePath)
    }

    override fun stopRecording() {
        recorder.stopRecording()
    }
}