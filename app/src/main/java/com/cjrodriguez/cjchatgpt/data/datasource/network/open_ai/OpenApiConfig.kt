package com.cjrodriguez.cjchatgpt.data.datasource.network.open_ai

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.cjrodriguez.cjchatgpt.BuildConfig
import com.cjrodriguez.cjchatgpt.presentation.util.AiType.GEMINI
import com.google.ai.client.generativeai.GenerativeModel
import kotlin.time.Duration.Companion.seconds

object OpenApiConfig {

    fun getOpenAiModel(
        apiKey: String
    ) = OpenAI(
        token = apiKey,
        timeout = Timeout(socket = 60.seconds)
    )

//    val openai = OpenAI(
//        token = BuildConfig.OPEN_AI_API_KEY,
//        timeout = Timeout(socket = 60.seconds)
//    )
}