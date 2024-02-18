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

//val lightFontStyle = SpanStyle(fontSize = 16.sp, color = Color(0xFF81009E))
//val stringSize = RichTextStringStyle(
//    boldStyle = lightFontStyle,
//    italicStyle = lightFontStyle,
//    underlineStyle = lightFontStyle,
//    strikethroughStyle = lightFontStyle,
//    subscriptStyle = lightFontStyle,
//    superscriptStyle = lightFontStyle,
//    codeStyle = lightFontStyle,
//    linkStyle = lightFontStyle
//)