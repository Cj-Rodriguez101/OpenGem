package com.cjrodriguez.cjchatgpt.data.repository.chat

import androidx.paging.PagingData
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.presentation.util.AiType
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun getAndStoreOpenAiChatResponse(
        message: String,
        isNewChat: Boolean,
        topicId: String,
        isCurrentlyConnectedToInternet: Boolean,
        model: String
    ): Flow<DataState<String>>

    suspend fun getAndStoreGeminiResponse(
        message: String,
        isNewChat: Boolean,
        topicId: String,
        isCurrentlyConnectedToInternet: Boolean,
    ): Flow<DataState<String>>

    fun copyTextToClipboard(textToCopy: String): Flow<DataState<Unit>>

    fun setGptVersion(aiType: AiType)

    fun getGptVersion(): Flow<String>

    fun getAllChats(topicId: String): Flow<PagingData<Chat>>

    fun getSelectedTopicName(topicId: String): Flow<String?>
}