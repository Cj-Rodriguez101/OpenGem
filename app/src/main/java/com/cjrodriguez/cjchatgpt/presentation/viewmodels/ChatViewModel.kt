package com.cjrodriguez.cjchatgpt.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.ConnectivityObserver
import com.cjrodriguez.cjchatgpt.data.repository.chat.ChatRepository
import com.cjrodriguez.cjchatgpt.data.util.GPT_3
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.CopyTextToClipBoard
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.NewChat
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SendMessage
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SetGptVersion
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SetMessage
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents.SetTopicId
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.presentation.components.UiText
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _message: MutableStateFlow<String> = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _shouldContinueChatGeneration: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private val _messageSet: MutableStateFlow<Set<GenericMessageInfo>> = MutableStateFlow(setOf())
    val messageSet: StateFlow<Set<GenericMessageInfo>> = _messageSet

    private val _selectedTopicId: MutableStateFlow<String> = MutableStateFlow("")
    val selectedTopicId: StateFlow<String> = _selectedTopicId

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
    val errorMessage: StateFlow<UiText> = _errorMessage

    private val _upperLimit: MutableStateFlow<Int> = MutableStateFlow(500)
    val upperLimit: StateFlow<Int> = _upperLimit

    private val _wordCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val wordCount: StateFlow<Int> = _wordCount

    private val _isGpt3: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isGpt3: StateFlow<Boolean> = _isGpt3

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _cancellableCoroutineScope: MutableStateFlow<Job?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            launch {
                chatRepository.getGptVersion().collectLatest { gptVersion ->
                    _isGpt3.value = gptVersion == GPT_3
                }
            }

            launch {
                _shouldContinueChatGeneration.collectLatest {shouldCancel->
                    if(!shouldCancel){
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

            is ChatListEvents.CancelChatGeneration->{
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
                setGptVersion()
            }

            is ChatListEvents.RemoveHeadMessage -> {
                removeHeadMessageFromQueue()
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

        displayErrorMessage(textLength)
    }

    private fun setGptVersion() {

        if (!isGpt3.value) {
            _upperLimit.value = 500
        } else {
            _upperLimit.value = 10000
        }
        displayErrorMessage(message.value.length)
        chatRepository.setGptVersion(!_isGpt3.value)
        _isGpt3.value = !_isGpt3.value
    }

    private fun displayErrorMessage(textLength: Int) {
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
                chatRepository.getAndStoreChatResponse(
                    message = _message.value.trim(), isNewChat = isNewChat,
                    isCurrentlyConnectedToInternet = status == ConnectivityObserver.Status.Available,
                    topicId = _selectedTopicId.value, model = chatRepository.getGptVersion().first()
                ).cancellable().collectLatest { dataState ->

                    _isLoading.value = dataState.isLoading

                    _message.value = ""
                    _wordCount.value = 0

                    dataState.data.let { data ->

                        data?.let { _selectedTopicId.value = it }

                    }

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