package com.cjrodriguez.cjchatgpt.presentation.navigation

import com.cjrodriguez.cjchatgpt.data.util.CHAT_SCREEN
import com.cjrodriguez.cjchatgpt.data.util.TOPIC_SCREEN

sealed class Screen(
    val route: String
){
    object ChatScreen: Screen(CHAT_SCREEN)
    object TopicScreen: Screen(TOPIC_SCREEN)
}