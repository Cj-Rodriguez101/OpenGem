package com.cjrodriguez.cjchatgpt.presentation.util

enum class AiType(
    val modelName: String,
    val displayName: String,
    val shouldBeVisible: Boolean = true) {
    GPT3("gpt-3.5-turbo", "GPT 3.5"),
    GPT4("gpt-4-0125-preview", "GPT 4"),
    DALL_E_3("dall-e-3", "DALL-E-3", false),
    GEMINI("gemini-pro", "GEMINI");

    companion object {
        fun fromModelName(modelName: String): AiType? {
            return values().find { it.modelName == modelName }
        }
    }
}