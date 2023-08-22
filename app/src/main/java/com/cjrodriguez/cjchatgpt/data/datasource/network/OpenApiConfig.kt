package com.cjrodriguez.cjchatgpt.data.datasource.network

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.cjrodriguez.cjchatgpt.BuildConfig
import kotlin.time.Duration.Companion.seconds

object OpenApiConfig {

    val openai = OpenAI(
        token = BuildConfig.API_KEY,
        timeout = Timeout(socket = 60.seconds)
    )
}