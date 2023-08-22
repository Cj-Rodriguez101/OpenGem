package com.cjrodriguez.cjchatgpt.data.datasource.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity

@Database(
    entities = [ChatEntity::class, TopicEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatTopicDao(): ChatTopicDao
}