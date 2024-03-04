package com.cjrodriguez.cjchatgpt.domain.model

import com.cjrodriguez.cjchatgpt.presentation.util.AiType

data class Chat(
    val messageId: String = "",
    val topicId: String,
    val content: String,
    val imageUrls: List<String>,
    val isUserGenerated: Boolean = true,
    val aiType: AiType,
    val lastCreatedIndex: Int = 0
)
