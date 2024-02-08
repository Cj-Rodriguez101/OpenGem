package com.cjrodriguez.cjchatgpt.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.ConnectivityObserver
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.NetworkConnectivityObserver
import com.cjrodriguez.cjchatgpt.presentation.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectivityObserver = NetworkConnectivityObserver(applicationContext)
        setContent {
            val status by connectivityObserver.observe()
                .collectAsStateWithLifecycle(initialValue = ConnectivityObserver.Status.Unavailable)
            Navigation(status)
        }
    }
}