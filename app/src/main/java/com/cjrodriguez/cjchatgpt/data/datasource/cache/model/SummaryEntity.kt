package com.cjrodriguez.cjchatgpt.data.datasource.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summaryTable")
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true) val summaryId: Int = 0,
    val topicId: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val content: ByteArray,
    val lastMaxTimeCreatedAt: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SummaryEntity

        if (summaryId != other.summaryId) return false
        if (topicId != other.topicId) return false
        if (!content.contentEquals(other.content)) return false
        return lastMaxTimeCreatedAt == other.lastMaxTimeCreatedAt
    }

    override fun hashCode(): Int {
        var result = summaryId
        result = 31 * result + topicId.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + lastMaxTimeCreatedAt
        return result
    }
}
