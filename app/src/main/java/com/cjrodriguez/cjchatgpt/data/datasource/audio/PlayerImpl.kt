package com.cjrodriguez.cjchatgpt.data.datasource.audio

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow

object PlayerImpl : Player {
    private var mediaPlayer: MediaPlayer? = null
    private val _isAudioPlayingFinished: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override fun playAudio(audioPath: String) {
        stopAudio()
        _isAudioPlayingFinished.value = false

        try {
            mediaPlayer = MediaPlayer().apply {
                setOnCompletionListener {
                    _isAudioPlayingFinished.value = true
                }
                setDataSource(audioPath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("player", e.toString())
        }
    }

    override fun stopAudio() {
        mediaPlayer?.apply {
            stop()
            reset()
            mediaPlayer?.release()
            mediaPlayer = null
        }
        _isAudioPlayingFinished.value = false
    }

    override fun getAudioFinished(): MutableStateFlow<Boolean> = _isAudioPlayingFinished
    override fun resetAudioPlayingState() {
        _isAudioPlayingFinished.value = false
    }
}