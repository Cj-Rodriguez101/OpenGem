package com.cjrodriguez.cjchatgpt.data.datasource.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summaryTable")
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true) val summaryId: Int = 0,
    val topicId: String,
    val content: String,
    val lastMaxTimeCreatedAt: Int = 0
)
