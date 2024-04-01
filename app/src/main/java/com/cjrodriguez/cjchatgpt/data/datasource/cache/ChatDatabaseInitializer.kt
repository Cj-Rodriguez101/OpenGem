package com.cjrodriguez.cjchatgpt.data.datasource.cache

import android.content.Context
import androidx.room.Room
import androidx.startup.Initializer
import com.cjrodriguez.cjchatgpt.data.util.CHAT_DB

class ChatDatabaseInitializer : Initializer<ChatDatabase> {
    override fun create(context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context, ChatDatabase::class.java, CHAT_DB
        ).fallbackToDestructiveMigration().build()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}