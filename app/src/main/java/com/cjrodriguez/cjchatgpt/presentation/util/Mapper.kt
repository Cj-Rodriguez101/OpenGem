package com.cjrodriguez.cjchatgpt.presentation.util

import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity
import com.cjrodriguez.cjchatgpt.data.util.dividePlainHtmlAndCode
import com.cjrodriguez.cjchatgpt.data.util.revertCodeOrPlainHtmlBackToHtml
import com.cjrodriguez.cjchatgpt.data.util.toByteArrayCustom
import com.cjrodriguez.cjchatgpt.data.util.transformTextToHtml
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.domain.model.Topic

fun ChatEntity.toChat(): Chat {
    return Chat(
        messageId = this.messageId,
        topicId = this.topicId,
        content = this.expandedContent.transformTextToHtml().dividePlainHtmlAndCode(),
        isUserGenerated = this.isUserGenerated,
        lastCreatedIndex = this.lastCreatedIndex
    )
}

fun TopicEntity.toTopic(): Topic {
    return Topic(id = id, title)
}

fun Topic.toTopicEntity(): TopicEntity {
    return TopicEntity(id = id, title)
}
