package com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cjrodriguez.cjchatgpt.data.repository.topic.TopicRepository
import com.cjrodriguez.cjchatgpt.data.util.SUCCESS
import com.cjrodriguez.cjchatgpt.domain.events.TopicListEvents
import com.cjrodriguez.cjchatgpt.domain.model.Topic
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.toTopicEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TopicViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val topicRepository: TopicRepository,
    private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _query: MutableStateFlow<String> = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val topicId: String = checkNotNull(savedStateHandle["topicId"])

    private val _currentTopicId: MutableStateFlow<String> =
        MutableStateFlow(topicId.replace("{", "").replace("}", ""))
    val currentTopicId: StateFlow<String> = _currentTopicId

    private val _messageSet: MutableStateFlow<Set<GenericMessageInfo>> = MutableStateFlow(setOf())
    val messageSet: StateFlow<Set<GenericMessageInfo>> = _messageSet

    @OptIn(ExperimentalCoroutinesApi::class)
    val topicPagingFlow: Flow<PagingData<Topic>> =
        _query.flatMapLatest {
            topicRepository.searchTopics(it)
        }.cachedIn(viewModelScope)

    fun onTriggerEvent(events: TopicListEvents) {
        when (events) {
            is TopicListEvents.SetQuery -> {
                setQuery(events.query)
            }

            is TopicListEvents.SetTopic->{
                _currentTopicId.value = events.topicId
            }

            is TopicListEvents.DeleteTopic -> {
                deleteTopic(events.topicId)
            }

            is TopicListEvents.RenameTopic -> {
                renameTopic(events.topic)
            }

            is TopicListEvents.OnRemoveHeadMessageFromQueue -> {
                removeHeadMessageFromQueue()
            }
        }
    }

    private fun deleteTopic(topicId: String) {
        viewModelScope.launch {
            withContext(coroutineDispatcher) {
                topicRepository.deleteTopic(topicId).collectLatest { dataState ->

                    dataState.data?.let {
                        if (it == SUCCESS && _currentTopicId.value == topicId){
                            _currentTopicId.value = ""
                        }
                    }

                    dataState.message?.let { message ->
                        appendToMessageQueue(message)
                    }

                }
            }
        }
    }

    private fun renameTopic(topic: Topic) {
        viewModelScope.launch {
            withContext(coroutineDispatcher) {
                topicRepository.renameTopic(topic.toTopicEntity()).collectLatest { dataState ->

                    dataState.message?.let { message ->
                        appendToMessageQueue(message)
                    }

                }
            }
        }
    }

    private fun setQuery(message: String) {
        _query.value = message
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