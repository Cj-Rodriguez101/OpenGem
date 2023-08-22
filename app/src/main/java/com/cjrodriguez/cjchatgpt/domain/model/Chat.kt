package com.cjrodriguez.cjchatgpt.domain.model


data class Chat(
    val messageId: String = "",
    val topicId: String,
    val content: List<Pair<String, Boolean>>,
    val isUserGenerated: Boolean = true,
    val lastCreatedIndex: Int = 0
)
