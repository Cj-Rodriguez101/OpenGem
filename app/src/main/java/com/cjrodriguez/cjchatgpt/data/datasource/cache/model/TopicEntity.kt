package com.cjrodriguez.cjchatgpt.data.datasource.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topicTable")
data class TopicEntity(
    @PrimaryKey val id: String = "",
    val title: String
)
