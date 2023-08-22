package com.cjrodriguez.cjchatgpt.data.repository.chat

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.dataStore.SettingsDataStore
import com.cjrodriguez.cjchatgpt.data.interactors.CopyTextToClipBoard
import com.cjrodriguez.cjchatgpt.data.interactors.GetChatResponse
import com.cjrodriguez.cjchatgpt.data.util.GPT_3
import com.cjrodriguez.cjchatgpt.data.util.GPT_4
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.toChat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val getChatResponse: GetChatResponse,
    private val copyTextToClipBoard: CopyTextToClipBoard,
    private val dao: ChatTopicDao, //remove this later
    private val settingsDataStore: SettingsDataStore
) : ChatRepository {
    override suspend fun getAndStoreChatResponse(
        message: String,
        isNewChat: Boolean,
        topicId: String,
        isCurrentlyConnectedToInternet: Boolean,
        model: String
    ): Flow<DataState<String>> {
        return getChatResponse.execute(message, isNewChat, isCurrentlyConnectedToInternet,
            topicId, model)
    }


    override fun copyTextToClipboard(textToCopy: String): Flow<DataState<Unit>> {
        return copyTextToClipBoard.execute(textToCopy)
    }

    override fun setGptVersion(isGpt3: Boolean) {
        settingsDataStore.writeGptVersion(if (isGpt3) GPT_3 else GPT_4)
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
}