package com.cjrodriguez.cjchatgpt.data.interactors

import android.content.Context
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RenameTopic @Inject constructor(
    private val context: Context,
    private val dao: ChatTopicDao
) {

    fun execute(topicEntity: TopicEntity): Flow<DataState<Unit>> = flow {

        var errorMessage = ""

        try {
            dao.insertTopic(topicEntity)
        } catch (ex: Exception) {
            errorMessage = ex.message.toString()
        }

        if (errorMessage.isEmpty()) {
            emit(
                DataState.data(
                    message = GenericMessageInfo
                        .Builder().id("RenameTopic.Success")
                        .title(context.getString(R.string.successfully_renamed_topic))
                        .uiComponentType(UIComponentType.SnackBar)
                )
            )
        } else {
            emit(
                DataState.error(
                    message = GenericMessageInfo
                        .Builder().id("RenameTopic.Error")
                        .title(context.getString(R.string.error))
                        .description(errorMessage)
                        .uiComponentType(UIComponentType.Dialog)
                )
            )
        }
    }
}