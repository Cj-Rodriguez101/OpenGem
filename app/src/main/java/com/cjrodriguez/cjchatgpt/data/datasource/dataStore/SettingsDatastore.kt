package com.cjrodriguez.cjchatgpt.data.datasource.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cjrodriguez.cjchatgpt.data.util.GPT_3
import com.cjrodriguez.cjchatgpt.data.util.GPT_SETTINGS
import com.cjrodriguez.cjchatgpt.data.util.GPT_VERSION_KEY
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
constructor(private val application: BaseApplication) {

    private val gptVersionKey = stringPreferencesKey(GPT_VERSION_KEY)

    val gptVersionFlow: Flow<String> = application.applicationContext.dataStore.data
        .map { preferences ->
            preferences[gptVersionKey] ?: GPT_3
        }.flowOn(IO)

    fun writeGptVersion(gptVersion: String) {
        runBlocking {
            application.applicationContext.dataStore.edit { settings ->
                settings[gptVersionKey] = gptVersion
            }
        }
    }
}