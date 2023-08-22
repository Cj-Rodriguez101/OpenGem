package com.cjrodriguez.cjchatgpt.data.repository.topic

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity
import com.cjrodriguez.cjchatgpt.data.interactors.DeleteTopicAndChats
import com.cjrodriguez.cjchatgpt.data.interactors.RenameTopic
import com.cjrodriguez.cjchatgpt.domain.model.Topic
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.toTopic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TopicRepositoryImpl @Inject constructor(
    private val dao: ChatTopicDao,
    private val deleteTopicAndChats: DeleteTopicAndChats,
    private val renameTopic: RenameTopic
): TopicRepository {

    override fun searchTopics(query: String): Flow<PagingData<Topic>> {
        return Pager(config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            maxSize = 20 * 3
        ),
            pagingSourceFactory = { dao.searchTopics(query) }).flow
            .map { pagingData ->
                pagingData.map { it.toTopic() }
            }
    }

    override fun deleteTopic(topicId: String): Flow<DataState<String>> {
        return deleteTopicAndChats.execute(topicId)
    }

    override fun renameTopic(topic: TopicEntity): Flow<DataState<Unit>> {
        return renameTopic.execute(topic)
    }
}