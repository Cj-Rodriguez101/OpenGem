package com.cjrodriguez.cjchatgpt.domain.events

sealed class SettingsEvents {

    data class SetOpenAiKey(val openAiKey: String) : SettingsEvents()
    data class SetGeminiKey(val geminiApiKey: String) : SettingsEvents()
    data class SaveApiKeys(
        val openAiKey: String,
        val geminiApiKey: String
    ) : SettingsEvents()

    data class ToggleHapticState(val shouldToggleHaptics: Boolean) : SettingsEvents()
    data object LoadApiKey : SettingsEvents()
    data object OnRemoveHeadMessage : SettingsEvents()
}