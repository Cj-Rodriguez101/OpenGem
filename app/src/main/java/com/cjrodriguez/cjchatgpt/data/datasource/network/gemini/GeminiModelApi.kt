package com.cjrodriguez.cjchatgpt.data.datasource.network.gemini

import com.cjrodriguez.cjchatgpt.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

object GeminiModelApi {
    fun getGenerativeModel(model: String = "gemini-pro") = GenerativeModel(
        modelName = model,
        apiKey = BuildConfig.GEMINI_AI_API_KEY
    )
}