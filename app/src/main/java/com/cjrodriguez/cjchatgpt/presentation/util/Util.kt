package com.cjrodriguez.cjchatgpt.presentation.util

fun <T> tryCatch(input: T): String {
    var errorMessage = ""
    try {
        input
    } catch (ex: Exception) {
        errorMessage = ex.message.toString()
    }
    return errorMessage
}