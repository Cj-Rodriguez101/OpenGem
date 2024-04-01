package com.cjrodriguez.cjchatgpt.presentation.screens.settingScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cjrodriguez.cjchatgpt.data.repository.settings.SettingsRepository
import com.cjrodriguez.cjchatgpt.domain.events.SettingsEvents
import com.cjrodriguez.cjchatgpt.domain.events.SettingsEvents.LoadApiKey
import com.cjrodriguez.cjchatgpt.domain.events.SettingsEvents.SaveApiKeys
import com.cjrodriguez.cjchatgpt.domain.events.SettingsEvents.ToggleHapticState
import com.cjrodriguez.cjchatgpt.domain.events.SettingsEvents.SetGeminiKey
import com.cjrodriguez.cjchatgpt.domain.events.SettingsEvents.SetOpenAiKey
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _messageSet: MutableStateFlow<Set<GenericMessageInfo>> = MutableStateFlow(setOf())
    val messageSet = _messageSet.asStateFlow()

    val shouldEnableHaptics = settingsRepository.collectHapticFeedbackState()

    private val _openAiKey: MutableStateFlow<String> = MutableStateFlow("")
    val openAiKey = _openAiKey.asStateFlow()

    private val _geminiKey: MutableStateFlow<String> = MutableStateFlow("")
    val geminiKey = _geminiKey.asStateFlow()

    init {
        viewModelScope.launch {
            val apiKeys = settingsRepository.getApiKeys()
            _openAiKey.value = apiKeys.openAiKey
            _geminiKey.value = apiKeys.geminiApiKey
        }
    }

    fun onTriggerEvent(events: SettingsEvents) {
        when (events) {

            is SetOpenAiKey -> {
                _openAiKey.value = events.openAiKey
            }

            is SetGeminiKey -> {
                _geminiKey.value = events.geminiApiKey
            }

            is SaveApiKeys -> {
                viewModelScope.launch {
                    settingsRepository.writeOpenAiKey(events.openAiKey)
                    settingsRepository.writeGeminiKey(events.geminiApiKey)
                }
            }

            is SettingsEvents.OnRemoveHeadMessage -> {
                removeHeadMessageFromQueue()
            }

            is ToggleHapticState -> {
                settingsRepository.writeHapticFeedbackState(events.shouldToggleHaptics)
            }

            LoadApiKey -> {
                viewModelScope.launch {
                    val apiKeys = settingsRepository.getApiKeys()
                    _openAiKey.value = apiKeys.openAiKey
                    _geminiKey.value = apiKeys.geminiApiKey
                }
            }
        }
    }

    private fun removeHeadMessageFromQueue() {
        try {
            if (_messageSet.value.isNotEmpty()) {
                val list = _messageSet.value.toMutableList()

                if (list.isNotEmpty()) {
                    list.removeAt(list.size - 1)
                }

                _messageSet.value = if (list.isEmpty()) setOf() else list.toSet()
            }
        } catch (ex: Exception) {
            Log.e("removeMessage", ex.toString())
        }
    }

}