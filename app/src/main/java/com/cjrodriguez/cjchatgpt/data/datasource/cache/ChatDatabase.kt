package com.cjrodriguez.cjchatgpt.data.datasource.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.SummaryEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity

@Database(
    entities = [ChatEntity::class, TopicEntity::class, SummaryEntity::class],
    version = 1,
    exportSchema = false
)
//@TypeConverters(value = [DataConverters::class])
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatTopicDao(): ChatTopicDao
}