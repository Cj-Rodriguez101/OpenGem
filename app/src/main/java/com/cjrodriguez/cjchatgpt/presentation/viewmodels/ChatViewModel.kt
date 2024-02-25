package com.cjrodriguez.cjchatgpt.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.ConnectivityObserver
import com.cjrodriguez.cjchatgpt.data.repository.chat.ChatRepository
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.CopyTextToClipBoard
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.NewChat
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SendMessage
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SetGptVersion
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SetMessage
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SetRecordingState
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SetShouldShowVoiceSegment
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SetTopicId
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.StartRecording
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.StopRecording
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.UpdatePowerLevel
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.presentation.components.UiText
import com.cjrodriguez.cjchatgpt.presentation.util.AiType
import com.cjrodriguez.cjchatgpt.presentation.util.AiType.GPT3
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.RecordingState
import com.cjrodriguez.cjchatgpt.presentation.util.RecordingState.ERROR
import com.cjrodriguez.cjchatgpt.presentation.util.RecordingState.FINISHED
import com.cjrodriguez.cjchatgpt.presentation.util.RecordingState.PROCESSING
import com.cjrodriguez.cjchatgpt.presentation.util.RecordingState.RECORDING
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _message: MutableStateFlow<String> = MutableStateFlow("")
    val message = _message.asStateFlow()

    private val _shouldContinueChatGeneration: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private val _messageSet: MutableStateFlow<Set<GenericMessageInfo>> = MutableStateFlow(setOf())
    val messageSet = _messageSet.asStateFlow()

    private val _shouldShowRecordingScreen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val shouldShowRecordingScreen = _shouldShowRecordingScreen.asStateFlow()

    private val _selectedTopicId: MutableStateFlow<String> = MutableStateFlow("")
    val selectedTopicId = _selectedTopicId.asStateFlow()

    val powerLevel = chatRepository.getPowerLevelWithListening().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0f
    )

    private val _recordingState: MutableStateFlow<RecordingState> = MutableStateFlow(RECORDING)
    val recordingState = _recordingState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatPagingFlow: Flow<PagingData<Chat>> =
        _selectedTopicId.flatMapLatest {
            chatRepository.getAllChats(it)
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val topicTitle: Flow<String?> =
        _selectedTopicId.flatMapLatest {
            chatRepository.getSelectedTopicName(it)
        }

    private val _errorMessage: MutableStateFlow<UiText> = MutableStateFlow(UiText.DynamicString(""))
    val errorMessage = _errorMessage.asStateFlow()

    private val _wordCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val wordCount = _wordCount.asStateFlow()

    val aiType = chatRepository.getGptVersion().map { AiType.valueOf(it) }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = GPT3
    )

    val upperLimit = aiType.map {
        if (it == GPT3) 500 else 10000
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 500
    )

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _cancellableCoroutineScope: MutableStateFlow<Job?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            launch {
                _shouldContinueChatGeneration.collectLatest { shouldCancel ->
                    if (!shouldCancel) {
                        _cancellableCoroutineScope.value?.let {
                            it.cancel()
                            it.cancelChildren()
                            _isLoading.value = false
                        }
                        _shouldContinueChatGeneration.value = true
                    }
                }
            }
        }
    }

    fun onTriggerEvent(events: ChatListEvents) {
        when (events) {

            is ChatListEvents.CancelChatGeneration -> {
                _shouldContinueChatGeneration.value = false
            }

            is CopyTextToClipBoard -> {
                copyTextToClipBoard(events.messageToCopy)
            }

            is NewChat -> {
                _selectedTopicId.value = ""
            }

            is SetMessage -> {
                setMessage(events.message)
            }

            is SendMessage -> {
                sendMessage(events.isCurrentlyConnectedToInternet)
            }

            is SetTopicId -> {
                _selectedTopicId.value = events.topicId
            }

            is SetGptVersion -> {
                setGptVersion(events.aiType)
            }

            is SetShouldShowVoiceSegment -> {
                _shouldShowRecordingScreen.value = events.shouldShowVoiceSegment
            }

            is ChatListEvents.RemoveHeadMessage -> {
                removeHeadMessageFromQueue()
            }

            is StartRecording -> {
                chatRepository.startRecording()
                _recordingState.value = RECORDING
            }

            is SetRecordingState -> {
                chatRepository.setRecordingState(events.isRecordingState)
            }

            is StopRecording -> {
                chatRepository.stopRecording()
                _recordingState.value = PROCESSING
                getTextToSpeech()
            }

            is UpdatePowerLevel -> {
                viewModelScope.launch {
                    chatRepository.updatePowerLevel()
                }
            }

            else -> Unit
        }
    }

    private fun getTextToSpeech() {
        viewModelScope.launch {
            withContext(coroutineDispatcher) {
                chatRepository.getTextFromSpeech().collectLatest { dataState ->
                    _isLoading.value = dataState.isLoading
                    _recordingState.value = PROCESSING

                    _message.value = ""
                    _wordCount.value = 0

                    dataState.data?.let {
                        _message.value = it
                        _recordingState.value = FINISHED
                        _shouldShowRecordingScreen.value = false
                    }

                    dataState.message?.let {
                        appendToMessageQueue(it)
                        _recordingState.value = ERROR
                        _shouldShowRecordingScreen.value = false
                    }
                }
            }
        }
    }

    private fun copyTextToClipBoard(textToCopy: String) {
        viewModelScope.launch {
            chatRepository.copyTextToClipboard(textToCopy).collectLatest { dataState ->

                dataState.message?.let {
                    appendToMessageQueue(it)
                }
            }
        }
    }

    private fun setMessage(message: String) {
        val textLength = message.length
        _wordCount.value = textLength
        _message.value = message

        setTextLength(textLength)
    }

    private fun setGptVersion(aiType: AiType) {
        setTextLength(message.value.length)
        chatRepository.setGptVersion(aiType)
    }

    private fun setTextLength(textLength: Int) {
        if (textLength > upperLimit.value) {
            _errorMessage.value = UiText.StringResource(resId = R.string.text_is_too_long)
        } else {
            _errorMessage.value = UiText.DynamicString("")
        }
    }

    private fun sendMessage(status: ConnectivityObserver.Status) {
        var isNewChat = false
        if (_selectedTopicId.value.isEmpty()) {
            _selectedTopicId.value = generateRandomId()
            isNewChat = true
        }

        _cancellableCoroutineScope.value = viewModelScope.launch {
            withContext(coroutineDispatcher) {
                val messageToSend = message.value.trim()
                val topicIdToSend = _selectedTopicId.value
                val isConnectedToInternet = status == ConnectivityObserver.Status.Available

                val responseFlow = when (aiType.value) {
                    AiType.GEMINI -> chatRepository.getAndStoreGeminiResponse(
                        message = messageToSend,
                        isNewChat = isNewChat,
                        isCurrentlyConnectedToInternet = isConnectedToInternet,
                        topicId = topicIdToSend
                    )

                    else -> chatRepository.getAndStoreOpenAiChatResponse(
                        message = messageToSend,
                        isNewChat = isNewChat,
                        isCurrentlyConnectedToInternet = isConnectedToInternet,
                        topicId = topicIdToSend,
                        model = aiType.value.modelName
                    )
                }

                responseFlow.cancellable().collectLatest { dataState ->
                    _isLoading.value = dataState.isLoading

                    _message.value = ""
                    _wordCount.value = 0

                    dataState.data?.let { _selectedTopicId.value = it }

                    dataState.message?.let {
                        appendToMessageQueue(it)
                    }
                }
            }
        }
    }

    private fun appendToMessageQueue(messageInfo: GenericMessageInfo.Builder) {
        if (!_messageSet.value.contains(messageInfo.build())) {
            val currentSet = _messageSet.value.toMutableSet()
            currentSet.add(messageInfo.build())
            _messageSet.value = currentSet
        }
    }

    private fun removeHeadMessageFromQueue() {
        try {
            if (_messageSet.value.isNotEmpty()) {
                val list = _messageSet.value.toMutableList()

                if (list.isNotEmpty()) {
                    list.removeAt(list.size - 1)
                }

                _messageSet.value = if (list.isEmpty()) setOf() else list.toSet()
            }
        } catch (ex: Exception) {
            Log.e("removeMessage", ex.toString())
        }
    }
}