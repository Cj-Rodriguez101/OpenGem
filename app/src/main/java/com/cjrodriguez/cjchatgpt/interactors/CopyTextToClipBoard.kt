package com.cjrodriguez.cjchatgpt.interactors

import android.content.ClipData
import android.content.ClipboardManager
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.util.PASSWORD
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import com.cjrodriguez.cjchatgpt.presentation.BaseApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CopyTextToClipBoard @Inject constructor(
    private val clipboardManager: ClipboardManager,
    private val baseApplication: BaseApplication,
) {

    fun execute(messageToCopy: String): Flow<DataState<Unit>> = flow {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(PASSWORD, messageToCopy))
        emit(
            DataState.data(message = GenericMessageInfo
            .Builder().id("CopyTextToClipBoard.Success")
            .title(baseApplication.applicationContext.getString(R.string.text_copied_to_clipboard))
            .uiComponentType(UIComponentType.SnackBar)))
    }
}