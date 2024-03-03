package com.cjrodriguez.cjchatgpt.data.datasource.network.gemini

import com.cjrodriguez.cjchatgpt.BuildConfig
import com.cjrodriguez.cjchatgpt.presentation.util.AiType.GEMINI
import com.google.ai.client.generativeai.GenerativeModel

object GeminiModelApi {
    fun getGenerativeModel(model: String = GEMINI.modelName) = GenerativeModel(
        modelName = model,
        apiKey = BuildConfig.GEMINI_AI_API_KEY
    )
}