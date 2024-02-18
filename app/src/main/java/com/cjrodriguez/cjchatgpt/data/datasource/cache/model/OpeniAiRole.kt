package com.cjrodriguez.cjchatgpt.data.datasource.cache.model

import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.core.Role

enum class OpeniAiRole(val roleName: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    FUNCTION("function"),
    TOOL("tool")
}

fun OpeniAiRole.getChatRole(): Role {
    return when (this.roleName) {
        "system" -> ChatRole.System
        "user" -> ChatRole.User
        "assistant" -> ChatRole.Assistant
        "function" -> ChatRole.Function
        else -> ChatRole.Tool
    }
}