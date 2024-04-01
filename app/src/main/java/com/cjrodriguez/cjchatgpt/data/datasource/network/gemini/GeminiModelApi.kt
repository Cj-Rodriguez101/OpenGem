package com.cjrodriguez.cjchatgpt.data.datasource.network.gemini

import com.cjrodriguez.cjchatgpt.BuildConfig
import com.cjrodriguez.cjchatgpt.presentation.util.AiType.GEMINI
import com.google.ai.client.generativeai.GenerativeModel

object GeminiModelApi {
    fun getGenerativeModel(
        model: String = GEMINI.modelName,
        apiKey: String
    ) = GenerativeModel(
        modelName = model,
        apiKey = apiKey
    )
}