package com.cjrodriguez.cjchatgpt.domain.events

import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.ConnectivityObserver

sealed class ChatListEvents {

    object SetGptVersion: ChatListEvents()
    data class SendMessage(val isCurrentlyConnectedToInternet: ConnectivityObserver.Status): ChatListEvents()
    object NewChat: ChatListEvents()
    data class CopyTextToClipBoard(val messageToCopy: String): ChatListEvents()
    object CancelChatGeneration: ChatListEvents()
    object RemoveHeadMessage: ChatListEvents()
    data class SetMessage(val message: String): ChatListEvents()
    data class SetTopicId(val topicId: String): ChatListEvents()
}