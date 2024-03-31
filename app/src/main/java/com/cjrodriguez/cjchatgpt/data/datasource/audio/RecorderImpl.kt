package com.cjrodriguez.cjchatgpt.data.datasource.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class RecorderImpl @Inject constructor(
    private val context: Context,
) : Recorder {
    private var mediaRecorder: MediaRecorder? = null
    private val _powerFlow: MutableStateFlow<Float> = MutableStateFlow(0f)
    private val _isRecording: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var silenceStartTime: Long? = null

    override fun startRecording(fileName: String) {
        try {
            silenceStartTime = null
            mediaRecorder = if (VERSION.SDK_INT >= VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(FileOutputStream(File(context.cacheDir, "$fileName.mp3")).fd)
                prepare()
            }
            mediaRecorder?.start()

        } catch (ex: Exception) {
            Log.e("mediaRecorder", ex.message.toString())
        }
    }

    override fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (ex: Exception) {
            Log.e("mediaRecorder", ex.message.toString())
        }
    }

    override fun setRecordingState(isRecording: Boolean) {
        _isRecording.value = isRecording
    }

    override fun getPowerLevel(): StateFlow<Float> = _powerFlow.asStateFlow()

    override fun updatePowerLevel(timeOut: Long): Flow<Boolean> = flow {
        var shouldStopRecording = false
        while (_isRecording.value) {
            delay(100L)
            try {
                mediaRecorder?.maxAmplitude?.let {
                    val normalizedValue = ((it / 32767f) * (500 - 100)) + 100
                    if (it <= 1000 && silenceStartTime == null) {
                        silenceStartTime = System.currentTimeMillis()
                    } else if (it <= 1000 && System.currentTimeMillis() - silenceStartTime!! >= timeOut) {
                        _isRecording.value = false
                        shouldStopRecording = true
                        silenceStartTime = null
                    }
                    _powerFlow.update { normalizedValue }
                }
            } catch (ex: Exception) {
                Log.e("mediaRecorder", ex.message.toString())
            } finally {
                emit(shouldStopRecording)
            }
        }
    }
}
