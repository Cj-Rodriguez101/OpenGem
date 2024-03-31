package com.cjrodriguez.cjchatgpt.domain.events

import android.net.Uri
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.ConnectivityObserver
import com.cjrodriguez.cjchatgpt.presentation.util.AiType
import com.cjrodriguez.cjchatgpt.presentation.util.SpeakingState

sealed class ChatListEvents {

    data class SetGptVersion(val aiType: AiType) : ChatListEvents()
    data class SendMessage(
        val isCurrentlyConnectedToInternet: ConnectivityObserver.Status,
        val fileUris: List<String>
    ) : ChatListEvents()

    data class StartVoiceChat(
        val isNewChat: Boolean = true,
        val isCurrentlyConnectedToInternet: ConnectivityObserver.Status,
    ) : ChatListEvents()

    data class SetRecordingState(val isRecordingState: Boolean) : ChatListEvents()

    data class SetShouldShowVoiceSegment(val shouldShowVoiceSegment: Boolean) : ChatListEvents()

    data object NewChat : ChatListEvents()
    data class CopyTextToClipBoard(val messageToCopy: String) : ChatListEvents()
    data class AddImage(val messagesToCopy: List<Uri>) : ChatListEvents()
    data class RemoveImage(val messageToCopy: Uri) : ChatListEvents()
    data object ClearAllImageAndText : ChatListEvents()
    data object CancelChatGeneration : ChatListEvents()
    data object RemoveHeadMessage : ChatListEvents()
    data class StartRecording(val fileName: String = "tmp") : ChatListEvents()
    data class SaveFile(val imagePath: String) : ChatListEvents()
    data class SetSpeakingState(val recordingState: SpeakingState) : ChatListEvents()
    data class SetZoomedImageUrl(val imagePath: String) : ChatListEvents()
    data class UpdatePowerLevel(val timeout: Long = 5000L) : ChatListEvents()
    data class StopRecording(val shouldGetResponse: Boolean = true) : ChatListEvents()
    data object ResetAudioPlayingState : ChatListEvents()
    data class SetMessage(val message: String) : ChatListEvents()
    data class SetTopicId(val topicId: String) : ChatListEvents()
}