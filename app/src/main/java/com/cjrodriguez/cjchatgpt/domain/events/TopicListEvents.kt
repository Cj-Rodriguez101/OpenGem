package com.cjrodriguez.cjchatgpt.domain.events

import com.cjrodriguez.cjchatgpt.domain.model.Topic

sealed class TopicListEvents {

    object OnRemoveHeadMessageFromQueue: TopicListEvents()
    data class SetQuery(val query: String): TopicListEvents()
    data class SetTopic(val topicId: String): TopicListEvents()
    data class DeleteTopic(val topicId: String): TopicListEvents()
    data class RenameTopic(val topic: Topic): TopicListEvents()
}