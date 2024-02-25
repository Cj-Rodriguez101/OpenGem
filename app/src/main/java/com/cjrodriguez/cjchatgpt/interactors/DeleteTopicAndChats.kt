package com.cjrodriguez.cjchatgpt.interactors

import android.content.Context
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.util.SUCCESS
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class DeleteTopicAndChats @Inject constructor(
    private val context: Context,
    private val dao: ChatTopicDao,
) {

    fun execute(topicId: String): Flow<DataState<String>> = flow {

        var errorMessage = ""

        try {
            deleteTopicIdFolder(context, topicId)
            dao.deleteTopicAndMessagesWithTopicId(topicId)
        } catch (ex: Exception) {
            errorMessage = ex.message.toString()
        }

        if (errorMessage.isEmpty()) {
            emit(
                DataState.data(
                    data = SUCCESS,
                    message = GenericMessageInfo
                        .Builder().id("DeleteTopicAndChats.Success")
                        .title(context.getString(R.string.success))
                        .description(context.getString(R.string.successfully_deleted_topic))
                        .uiComponentType(UIComponentType.SnackBar)
                )
            )
        } else {
            emit(
                DataState.error(
                    message = GenericMessageInfo
                        .Builder().id("DeleteTopicAndChats.Error")
                        .title(context.getString(R.string.error))
                        .description(errorMessage)
                        .uiComponentType(UIComponentType.Dialog)
                )
            )
        }
    }

    private fun deleteTopicIdFolder(context: Context, topicId: String): Boolean {
        val imagesDir = context.getExternalFilesDir(null)?.absolutePath
        val topicDir = File(imagesDir, "images/$topicId")

        return if (topicDir.isDirectory) {
            topicDir.deleteRecursively()
        } else {
            false // The folder was not found or it's not a directory
        }
    }
}