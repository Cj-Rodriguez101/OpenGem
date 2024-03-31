package com.cjrodriguez.cjchatgpt.interactors

import android.annotation.SuppressLint
import android.content.Context
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.cjrodriguez.cjchatgpt.R.string
import com.cjrodriguez.cjchatgpt.data.datasource.network.open_ai.OpenApiConfig
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.buffer
import okio.source
import java.io.File
import java.io.IOException
import javax.inject.Inject

class GetTextFromSpeech @Inject constructor(
    private val context: Context,
    private val openAIConfig: OpenApiConfig
) {
    @SuppressLint("MissingPermission")
    fun execute(): Flow<DataState<String>> = flow {
        emit(DataState.loading())

        try {
            val fileToDeleted = File(context.cacheDir, "tmp.mp3")
            val source = fileToDeleted.getRecordingSource()
            val request = TranscriptionRequest(
                audio = FileSource(name = fileToDeleted.name, source = source),
                model = ModelId("whisper-1"),
            )

            val transcription = openAIConfig.openai.transcription(request)
            fileToDeleted.delete()
            emit(DataState.data(data = transcription.text))
        } catch (ex: Exception) {
            emit(
                DataState.error(
                    message = GenericMessageInfo
                        .Builder().id("GetTextFromSpeech.Error")
                        .title(context.getString(string.error))
                        .description(context.getString(string.unknown_error) + ex.toString())
                        .uiComponentType(UIComponentType.Dialog)
                )
            )
        }
    }
}

fun File.getRecordingSource(): okio.Source {
    if (!this.exists()) throw IOException("Recording file does not exist.")
    return this.source().buffer()
}