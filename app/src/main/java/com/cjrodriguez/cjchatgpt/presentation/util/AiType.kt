package com.cjrodriguez.cjchatgpt.presentation.util

const val OPEN_AI = "OpenAI"
const val GOOGLE = "Google"
enum class AiType(
    val modelName: String,
    val displayName: String,
    val familyName: String,
    val shouldBeVisible: Boolean = true
) {
    GPT3("gpt-3.5-turbo", "GPT 3.5", OPEN_AI),
    GPT4("gpt-4-0125-preview", "GPT 4", OPEN_AI),
    GPT4_VISION("gpt-4-vision-preview", "GPT 4 VISION", OPEN_AI, false),
    DALL_E_3("dall-e-3", "DALL-E-3", OPEN_AI, false),
    GEMINI("gemini-1.0-pro-latest", "GEMINI", GOOGLE),
    GEMINI_VISION("gemini-pro-vision", "GEMINI VISION", GOOGLE, false);

    companion object {
        fun fromModelName(modelName: String): AiType? {
            return values().find { it.modelName == modelName }
        }
    }
}