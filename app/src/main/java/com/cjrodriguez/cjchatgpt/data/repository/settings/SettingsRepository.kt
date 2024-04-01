package com.cjrodriguez.cjchatgpt.data.repository.settings

import com.cjrodriguez.cjchatgpt.BuildConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getApiKeys(): ApiKeys

    fun collectHapticFeedbackState(): Flow<Boolean>
    fun writeHapticFeedbackState(shouldVibrate: Boolean)
    fun writeOpenAiKey(openAiKey: String)
    fun writeGeminiKey(geminiApiKey: String)
}

data class ApiKeys(
    val openAiKey: String = BuildConfig.OPEN_AI_API_KEY,
    val geminiApiKey: String = BuildConfig.GEMINI_AI_API_KEY
)