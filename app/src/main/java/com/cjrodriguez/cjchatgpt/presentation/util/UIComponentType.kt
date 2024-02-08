package com.cjrodriguez.cjchatgpt.presentation.util

sealed class UIComponentType {

    object Dialog : UIComponentType()

    object SnackBar : UIComponentType()

    object None : UIComponentType()
}