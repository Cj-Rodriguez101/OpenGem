package com.cjrodriguez.cjchatgpt.data.repository.chat

import androidx.paging.PagingData
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun getAndStoreChatResponse(message: String, isNewChat: Boolean,
                                topicId: String, isCurrentlyConnectedToInternet: Boolean,
                                model: String): Flow<DataState<String>>

    fun copyTextToClipboard(textToCopy: String): Flow<DataState<Unit>>

    fun setGptVersion(isGpt3: Boolean)

    fun getGptVersion(): Flow<String>

    fun getAllChats(topicId: String): Flow<PagingData<Chat>>

    fun getSelectedTopicName(topicId: String): Flow<String?>
}