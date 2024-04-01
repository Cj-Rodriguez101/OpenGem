package com.cjrodriguez.cjchatgpt.data.datasource.dataStore

import android.content.Context
import androidx.startup.Initializer

class SettingsDataStoreInitializer : Initializer<SettingsDataStore> {
    override fun create(context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

}