package com.cjrodriguez.cjchatgpt.data.datasource.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.OpeniAiRole.USER

@Entity(tableName = "chatTable")
data class ChatEntity(
    @PrimaryKey val messageId: String = "",
    val topicId: String,
    val expandedContent: String = "",
    val imageUrls: List<String> = listOf(),
    val isUserGenerated: Boolean = true,
    val modelId: String = "gpt-3.5-turbo",
    val openAiChatRole: OpeniAiRole = USER,
    val lastCreatedIndex: Int = 0
)
