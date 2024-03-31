package com.cjrodriguez.cjchatgpt.data.datasource.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Recorder {

    fun startRecording(fileName: String)

    fun stopRecording()

    fun setRecordingState(isRecording: Boolean)

    fun getPowerLevel(): StateFlow<Float>

    fun updatePowerLevel(timeOut: Long): Flow<Boolean>
}