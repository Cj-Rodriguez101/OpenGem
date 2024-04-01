package com.cjrodriguez.cjchatgpt.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.cjrodriguez.cjchatgpt.presentation.screens.settingScreen.SettingScreen
import com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.TopicScreen
import com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.ChatViewModel
import com.cjrodriguez.cjchatgpt.presentation.screens.settingScreen.SettingsViewModel
import com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.TopicViewModel
import kotlinx.collections.immutable.toImmutableSet

@ExperimentalMaterial3Api
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
            val speakingState by chatViewModel.speakingState.collectAsStateWithLifecycle()
            val hasFinishedPlayingAudio by chatViewModel.hasFinishedPlayingAudio.collectAsStateWithLifecycle()
            ChatScreen(
                selectedAiType = selectedAi,
                allChats = allChats,
                message = message,
                wordCount = wordCount,
                upperLimit = upperLimit,
                errorMessage = errorMessage,
                status = status,
                isLoading = isLoading,
                speakingState = speakingState,
                messageSet = messageSet.toImmutableSet(),
                topicTitle = topicTitle ?: "",
                recordingState = recordingState,
                selectedFiles = selectedFiles,
                isRecordingScreenVisible = shouldShowVoiceSegment,
                circlePowerLevel = powerLevel,
                imageZoomedInPath = imageZoomedInPath,
                topicId = topicId,
                hasFinishedPlayingAudio = hasFinishedPlayingAudio,
                navigateToSettingsPage = { navController.navigate(route = Screen.SettingsScreen.route) },
                navigateToHistoryScreen =
                { navController.navigate(route = Screen.TopicScreen.route + "/{$it}") },
                onTriggerEvent = chatViewModel::onTriggerEvent
            )
        }
        composable(
            route = Screen.TopicScreen.route + "/{topicId}",
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
            enterTransition = { EnterTransition() },
            exitTransition = { ExitTransition() },
            popEnterTransition = { PopEnterTransition() },
            popExitTransition = { PopExitTransition() },
        ) {
            val topicViewModel = hiltViewModel<TopicViewModel>()
            val query by topicViewModel.query.collectAsStateWithLifecycle()
            val messageSet by topicViewModel.messageSet.collectAsStateWithLifecycle()
            TopicScreen(
                query = query,
                onTriggerEvents = topicViewModel::onTriggerEvent,
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

        composable(
            route = Screen.SettingsScreen.route,
            enterTransition = { EnterTransition() },
            exitTransition = { ExitTransition() },
            popEnterTransition = { PopEnterTransition() },
            popExitTransition = { PopExitTransition() },
        ) {
            val settingsViewModel = hiltViewModel<SettingsViewModel>()
            val openAiKey by settingsViewModel.openAiKey.collectAsStateWithLifecycle()
            val geminiKey by settingsViewModel.geminiKey.collectAsStateWithLifecycle()
            val messageSet by settingsViewModel.messageSet.collectAsStateWithLifecycle()
            val shouldEnableHaptics by settingsViewModel.shouldEnableHaptics.collectAsStateWithLifecycle(
                false
            )
            SettingScreen(
                openAiKey = openAiKey,
                geminiKey = geminiKey,
                onBackPressed = { navController.popBackStack() },
                onTriggerEvents = settingsViewModel::onTriggerEvent,
                messageSet = messageSet.toImmutableSet(),
                shouldEnableHaptics = shouldEnableHaptics
            )
        }
    }
}

private fun EnterTransition() = slideInHorizontally(
    initialOffsetX = { 1000 },
    animationSpec = tween(500)
)

private fun PopEnterTransition() = slideInHorizontally(
    initialOffsetX = { -1000 },
    animationSpec = tween(500)
)

private fun ExitTransition() = slideOutHorizontally(
    targetOffsetX = { -1000 },
    animationSpec = tween(500)
)

private fun PopExitTransition() = slideOutHorizontally(
    targetOffsetX = { 1000 },
    animationSpec = tween(500)
)