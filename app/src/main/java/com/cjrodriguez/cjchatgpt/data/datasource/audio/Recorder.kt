package com.cjrodriguez.cjchatgpt.data.datasource.audio

import kotlinx.coroutines.flow.StateFlow

interface Recorder {

    fun startRecording()

    fun stopRecording()

    fun setRecordingState(isRecording: Boolean)

    fun getPowerLevel(): StateFlow<Float>

    suspend fun updatePowerLevel()
}