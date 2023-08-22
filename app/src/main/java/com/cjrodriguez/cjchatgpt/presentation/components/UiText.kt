package com.cjrodriguez.cjchatgpt.presentation.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

//https://www.youtube.com/watch?v=mB1Lej0aDus&ab_channel=PhilippLackner
@Immutable
sealed class UiText {
    data class DynamicString(val value: String): UiText()
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ): UiText()

    @Composable
    fun asString(): String{
        return when(this){
            is DynamicString -> value
            is StringResource -> stringResource(id = resId, *args)
        }
    }
}