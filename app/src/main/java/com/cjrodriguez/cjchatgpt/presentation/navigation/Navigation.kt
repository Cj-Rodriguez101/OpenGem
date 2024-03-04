package com.cjrodriguez.cjchatgpt.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.ConnectivityObserver
import com.cjrodriguez.cjchatgpt.data.util.CHAT_KEY
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents
import com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.ChatScreen
import com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.TopicScreen
import com.cjrodriguez.cjchatgpt.presentation.viewmodels.ChatViewModel
import com.cjrodriguez.cjchatgpt.presentation.viewmodels.TopicViewModel
import kotlinx.collections.immutable.toImmutableSet

@Composable
fun Navigation(status: ConnectivityObserver.Status) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.ChatScreen.route) {
        composable(Screen.ChatScreen.route) { backStackEntry ->

            val chatViewModel = hiltViewModel<ChatViewModel>()
            val backStackTopicId by backStackEntry.savedStateHandle
                .getStateFlow<String?>(CHAT_KEY, null).collectAsStateWithLifecycle()

            LaunchedEffect(key1 = backStackTopicId) {
                backStackTopicId?.let {
                    chatViewModel.onTriggerEvent(
                        ChatListEvents.SetTopicId(it)
                    )
                    backStackEntry.savedStateHandle.remove<String>(CHAT_KEY)
                }
            }

            val message by chatViewModel.message.collectAsStateWithLifecycle()
            val topicTitle by chatViewModel.topicTitle.collectAsStateWithLifecycle(initialValue = "")
            val topicId by chatViewModel.selectedTopicId.collectAsStateWithLifecycle(initialValue = "")
            val selectedAi by chatViewModel.aiType.collectAsStateWithLifecycle()
            val wordCount by chatViewModel.wordCount.collectAsStateWithLifecycle()
            val upperLimit by chatViewModel.upperLimit.collectAsStateWithLifecycle()
            val errorMessage by chatViewModel.errorMessage.collectAsStateWithLifecycle()
            val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
            val messageSet by chatViewModel.messageSet.collectAsStateWithLifecycle()
            val shouldShowVoiceSegment by chatViewModel.shouldShowRecordingScreen.collectAsStateWithLifecycle()
            val imageZoomedInPath by chatViewModel.imageZoomedInPath.collectAsStateWithLifecycle()
            val recordingState by chatViewModel.recordingState.collectAsStateWithLifecycle()
            val powerLevel by chatViewModel.powerLevel.collectAsStateWithLifecycle()
            val allChats = chatViewModel.chatPagingFlow.collectAsLazyPagingItems()
            val selectedFiles by chatViewModel.selectedFiles.collectAsStateWithLifecycle()
            ChatScreen(
                selectedAiType = selectedAi,
                allChats = allChats,
                message = message,
                wordCount = wordCount,
                upperLimit = upperLimit,
                errorMessage = errorMessage,
                status = status,
                isLoading = isLoading,
                messageSet = messageSet.toImmutableSet(),
                topicTitle = topicTitle ?: "",
                recordingState = recordingState,
                selectedFiles = selectedFiles,
                shouldShowRecordingScreen = shouldShowVoiceSegment,
                circlePowerLevel = powerLevel,
                imageZoomedInPath = imageZoomedInPath,
                topicId = topicId,
                navigateToHistoryScreen =
                { navController.navigate(route = Screen.TopicScreen.route + "/{$it}") },
                onTriggerEvent = chatViewModel::onTriggerEvent
            )
        }
        composable(
            route = Screen.TopicScreen.route + "/{topicId}",
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) {
            val topicViewModel = hiltViewModel<TopicViewModel>()
            val query by topicViewModel.query.collectAsStateWithLifecycle()
            val messageSet by topicViewModel.messageSet.collectAsStateWithLifecycle()
            TopicScreen(query = query, onTriggerEvents = topicViewModel::onTriggerEvent,
                messageSet = messageSet.toImmutableSet(),
                allTopics = topicViewModel.topicPagingFlow.collectAsLazyPagingItems(),
                setIdToNavigateToAndOnBackPressed = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        CHAT_KEY,
                        topicViewModel.currentTopicId.value
                    )
                    navController.popBackStack()
                })
        }
    }
}