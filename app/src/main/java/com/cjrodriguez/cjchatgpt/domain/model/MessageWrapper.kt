package com.cjrodriguez.cjchatgpt.domain.model

data class MessageWrapper(
    val message: String,
    val fileUris: List<String> = listOf()
)
