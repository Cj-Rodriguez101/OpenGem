package com.cjrodriguez.cjchatgpt.domain.events

import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.ConnectivityObserver
import com.cjrodriguez.cjchatgpt.presentation.util.AiType

sealed class ChatListEvents {

    data class SetGptVersion(val aiType: AiType) : ChatListEvents()
    data class SendMessage(val isCurrentlyConnectedToInternet: ConnectivityObserver.Status) :
        ChatListEvents()

    data class SetRecordingState(val isRecordingState: Boolean) : ChatListEvents()

    data class SetShouldShowVoiceSegment(val shouldShowVoiceSegment: Boolean) : ChatListEvents()

    object NewChat : ChatListEvents()
    data class CopyTextToClipBoard(val messageToCopy: String) : ChatListEvents()
    object CancelChatGeneration : ChatListEvents()
    object RemoveHeadMessage : ChatListEvents()
    object StartRecording : ChatListEvents()
    data class SaveFile(val imagePath: String) : ChatListEvents()
    data class SetZoomedImageUrl(val imagePath: String) : ChatListEvents()

    object UpdatePowerLevel : ChatListEvents()
    object StopRecording : ChatListEvents()
    data class SetMessage(val message: String) : ChatListEvents()
    data class SetTopicId(val topicId: String) : ChatListEvents()
}