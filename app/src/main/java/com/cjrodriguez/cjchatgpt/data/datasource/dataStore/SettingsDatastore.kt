package com.cjrodriguez.cjchatgpt.data.datasource.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cjrodriguez.cjchatgpt.data.util.GEMINI_AI_KEY
import com.cjrodriguez.cjchatgpt.data.util.GPT_3
import com.cjrodriguez.cjchatgpt.data.util.GPT_SETTINGS
import com.cjrodriguez.cjchatgpt.data.util.GPT_VERSION_KEY
import com.cjrodriguez.cjchatgpt.data.util.HAPTIC_FEEDBACK_KEY
import com.cjrodriguez.cjchatgpt.data.util.OPEN_AI_KEY
import com.cjrodriguez.cjchatgpt.presentation.BaseApplication
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = GPT_SETTINGS)

class SettingsDataStore
@Inject
constructor(private val context: Context) {

    private val gptVersionKey = stringPreferencesKey(GPT_VERSION_KEY)
    private val openAiKey = stringPreferencesKey(OPEN_AI_KEY)
    private val geminiKey = stringPreferencesKey(GEMINI_AI_KEY)
    private val hapticFeedbackKey = booleanPreferencesKey(HAPTIC_FEEDBACK_KEY)

    val gptVersionFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[gptVersionKey] ?: GPT_3
        }.flowOn(IO)

    fun writeGptVersion(gptVersion: String) {
        runBlocking {
            context.dataStore.edit { settings ->
                settings[gptVersionKey] = gptVersion
            }
        }
    }

    val openAiKeyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[openAiKey] ?: ""
        }.flowOn(IO)

    fun writeOpenAiKey(openAiApiKey: String) {
        runBlocking {
            context.dataStore.edit { settings ->
                settings[openAiKey] = openAiApiKey
            }
        }
    }

    val geminiKeyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[geminiKey] ?: ""
        }.flowOn(IO)

    fun writeGeminiKey(geminiApiKey: String) {
        runBlocking {
            context.dataStore.edit { settings ->
                settings[geminiKey] = geminiApiKey
            }
        }
    }

    val hapticFeedbackFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[hapticFeedbackKey] ?: true
        }.flowOn(IO)

    fun writeHapticFeedbackState(shouldVibrate: Boolean) {
        runBlocking {
            context.dataStore.edit { settings ->
                settings[hapticFeedbackKey] = shouldVibrate
            }
        }
    }
}