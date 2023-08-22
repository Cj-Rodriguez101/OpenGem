package com.cjrodriguez.cjchatgpt.data.interactors

import android.content.Context
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeleteTopicAndChats @Inject constructor(
    private val context: Context,
    private val dao: ChatTopicDao,
    private val coroutineDispatcher: CoroutineDispatcher
) {

    fun execute(topicId: String): Flow<DataState<Unit>> = flow{

        var errorMessage = ""

        val job = CoroutineScope(coroutineDispatcher).launch {
            try {
                dao.deleteTopicAndMessagesWithTopicId(topicId)
            }catch (ex: Exception){
                errorMessage = ex.message.toString()
            }
        }
        
        job.join()
        
        if (errorMessage.isEmpty()){
            emit(
                DataState.data(message = GenericMessageInfo
                .Builder().id("DeleteTopicAndChats.Success")
                .title(context.getString(R.string.success))
                .description(context.getString(R.string.successfully_deleted_topic))
                .uiComponentType(UIComponentType.SnackBar)))
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
}