package com.cjrodriguez.cjchatgpt.data.repository.topic

import androidx.paging.PagingData
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity
import com.cjrodriguez.cjchatgpt.domain.model.Topic
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import kotlinx.coroutines.flow.Flow

interface TopicRepository {

    fun searchTopics(query: String): Flow<PagingData<Topic>>

    fun deleteTopic(topicId: String): Flow<DataState<String>>

    fun renameTopic(topic: TopicEntity): Flow<DataState<Unit>>
}