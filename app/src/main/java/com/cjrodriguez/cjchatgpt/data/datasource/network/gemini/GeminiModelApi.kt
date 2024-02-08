package com.cjrodriguez.cjchatgpt.data.datasource.network.gemini

import com.cjrodriguez.cjchatgpt.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

object GeminiModelApi {
    val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_AI_API_KEY
    )
}