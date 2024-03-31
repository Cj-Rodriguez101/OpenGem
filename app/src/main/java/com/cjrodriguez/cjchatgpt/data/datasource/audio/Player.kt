package com.cjrodriguez.cjchatgpt.data.datasource.audio

import kotlinx.coroutines.flow.MutableStateFlow

interface Player {
    fun playAudio(audioPath: String)
    fun stopAudio()
    fun getAudioFinished(): MutableStateFlow<Boolean>
    fun resetAudioPlayingState()
}