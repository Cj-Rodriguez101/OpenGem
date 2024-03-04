package com.cjrodriguez.cjchatgpt.presentation.util

enum class AiType(
    val modelName: String,
    val displayName: String,
    val shouldBeVisible: Boolean = true) {
    GPT3("gpt-3.5-turbo", "GPT 3.5"),
    GPT4("gpt-4-0125-preview", "GPT 4"),
    GPT4_VISION("gpt-4-vision-preview", "GPT 4 VISION", false),
    DALL_E_3("dall-e-3", "DALL-E-3", false),
    GEMINI("gemini-1.0-pro-latest", "GEMINI"),
    GEMINI_VISION("gemini-pro-vision", "GEMINI VISION", false);

    companion object {
        fun fromModelName(modelName: String): AiType? {
            return values().find { it.modelName == modelName }
        }
    }
}