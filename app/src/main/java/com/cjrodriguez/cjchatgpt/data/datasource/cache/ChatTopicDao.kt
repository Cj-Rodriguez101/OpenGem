package com.cjrodriguez.cjchatgpt.data.datasource.cache

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatTopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatResponse(chatEntity: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTopic(topicEntity: TopicEntity)

    @Query("DELETE FROM chatTable WHERE topicId =:topicId")
    fun deleteAllMessagesWithTopic(topicId: String)

    @Query("DELETE FROM topicTable WHERE id =:id")
    fun deleteTopicId(id: String)

    @Transaction
    fun deleteTopicAndMessagesWithTopicId(topicId: String){
        deleteTopicId(topicId)
        deleteAllMessagesWithTopic(topicId)
    }

    @Query("UPDATE chatTable SET expandedContent = expandedContent || :textToAppend WHERE messageId = :messageId")
    fun appendTextToContentMessage(messageId: String, textToAppend: String): Int

    @Query("UPDATE topicTable SET title = title || :textToAppend WHERE id = :topicId")
    fun appendTextToTopicTitle(topicId: String, textToAppend: String): Int

    @Query("SELECT * FROM chatTable WHERE topicId =:topicId ORDER BY lastCreatedIndex DESC")
    fun getAllChatsFromTopic(topicId: String): PagingSource<Int, ChatEntity>

    @Query("SELECT * FROM topicTable WHERE title LIKE '%' || :query || '%' OR  :query  = '' ORDER BY title COLLATE NOCASE ASC")
    fun searchTopics(query: String): PagingSource<Int, TopicEntity>

    @Query("SELECT * FROM topicTable LIMIT 1")
    fun getFirstTopic(): Flow<TopicEntity>

    @Query("SELECT MAX(lastCreatedIndex) FROM chatTable")
    fun getMaxTimeCreatedAt(): Int?

    @Query("SELECT title FROM topicTable WHERE id =:id LIMIT 1")
    fun getSpecificTopic(id: String): Flow<String?>
}