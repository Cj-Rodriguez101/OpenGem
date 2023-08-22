package com.cjrodriguez.cjchatgpt.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.ConnectivityObserver
import com.cjrodriguez.cjchatgpt.data.util.CHAT_KEY
import com.cjrodriguez.cjchatgpt.data.util.CHAT_SCREEN
import com.cjrodriguez.cjchatgpt.data.util.TOPIC_SCREEN
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents
import com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.ChatScreen
import com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.ChatViewModel
import com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.TopicScreen
import com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.TopicViewModel
import kotlinx.collections.immutable.toImmutableSet

@Composable
fun Navigation(status: ConnectivityObserver.Status) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "chatScreen") {
        composable(CHAT_SCREEN) { backStackEntry ->

            val chatViewModel = hiltViewModel<ChatViewModel>()

            LaunchedEffect(key1 = Unit) {
                backStackEntry.savedStateHandle.get<String>(CHAT_KEY)?.let {
                    chatViewModel.onTriggerEvent(
                        ChatListEvents.SetTopicId(it)
                    )
                    backStackEntry.savedStateHandle.remove<String>(CHAT_KEY)
                }
            }

            val message by chatViewModel.message.collectAsStateWithLifecycle()
            val topicTitle by chatViewModel.topicTitle.collectAsStateWithLifecycle(initialValue = "")
            val isGpt3 by chatViewModel.isGpt3.collectAsStateWithLifecycle()
            val wordCount by chatViewModel.wordCount.collectAsStateWithLifecycle()
            val upperLimit by chatViewModel.upperLimit.collectAsStateWithLifecycle()
            val errorMessage by chatViewModel.errorMessage.collectAsStateWithLifecycle()
            val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
            val messageSet by chatViewModel.messageSet.collectAsStateWithLifecycle()
            val allChats = chatViewModel.chatPagingFlow.collectAsLazyPagingItems()
            ChatScreen(
                isGpt3 = isGpt3,
                allChats = allChats,
                message = message,
                wordCount = wordCount,
                upperLimit = upperLimit,
                errorMessage = errorMessage,
                status = status,
                isLoading = isLoading,
                messageSet = messageSet.toImmutableSet(),
                topicTitle = topicTitle ?: "",
                navigateToHistoryScreen =
                {
                    navController.navigate(route = TOPIC_SCREEN)
                },
                onTriggerEvent = chatViewModel::onTriggerEvent
            )
        }
        composable(TOPIC_SCREEN) {
            val topicViewModel = hiltViewModel<TopicViewModel>()
            val query by topicViewModel.query.collectAsStateWithLifecycle()
            val messageSet by topicViewModel.messageSet.collectAsStateWithLifecycle()
            TopicScreen(query = query, onTriggerEvents = topicViewModel::onTriggerEvent,
                messageSet = messageSet.toImmutableSet(),
                allTopics = topicViewModel.topicPagingFlow.collectAsLazyPagingItems(),
                onBackPressed = { topicId ->
                    if (topicId.isNotEmpty()) {
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            CHAT_KEY,
                            topicId
                        )
                    }
                    navController.popBackStack()
                })
        }
    }
}