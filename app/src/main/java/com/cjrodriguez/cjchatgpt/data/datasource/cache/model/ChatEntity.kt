package com.cjrodriguez.cjchatgpt.data.datasource.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chatTable")
data class ChatEntity(
    @PrimaryKey val messageId: String = "",
    val topicId: String,
//    val content: ByteArray, use this before with base64 but since i collect text directly no need
    val expandedContent: String = "",
    val isUserGenerated: Boolean = true,
    val lastCreatedIndex: Int = 0
)
