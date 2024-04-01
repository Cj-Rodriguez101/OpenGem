package com.cjrodriguez.cjchatgpt.data.repository.settings

import com.cjrodriguez.cjchatgpt.data.datasource.dataStore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    val dataStore: SettingsDataStore
) : SettingsRepository {
    override suspend fun getApiKeys(): ApiKeys {
        val geminiKey = dataStore.geminiKeyFlow.first()
        val openKey = dataStore.openAiKeyFlow.first()
        return ApiKeys(
            openAiKey = openKey,
            geminiApiKey = geminiKey
        )
    }

    override fun collectHapticFeedbackState(): Flow<Boolean> {
        return dataStore.hapticFeedbackFlow
    }

    override fun writeHapticFeedbackState(shouldVibrate: Boolean) {
        dataStore.writeHapticFeedbackState(shouldVibrate)
    }

    override fun writeOpenAiKey(openAiKey: String) {
        dataStore.writeOpenAiKey(openAiKey)
    }

    override fun writeGeminiKey(geminiApiKey: String) {
        dataStore.writeGeminiKey(geminiApiKey)
    }
}