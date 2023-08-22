package com.cjrodriguez.cjchatgpt.data.util

import android.util.Base64

const val GPT_3 = "gpt-3.5-turbo"
const val GPT_4 = "gpt-4"
const val CHAT_DB = "Chat.db"
const val GPT_VERSION_KEY = "gptVersionKey"
const val CHAT_SCREEN = "chatScreen"
const val TOPIC_SCREEN = "topicScreen"
const val GPT_SETTINGS = "settings"
const val PRE_TAG = "pre-wrap"
const val CHAT_KEY = "key"

fun String.toByteArrayCustom(): ByteArray {
    return try {
//        val byteArrayStream = ByteArrayOutputStream(this.length)
//        val gzip = GZIPOutputStream(byteArrayStream)
//        gzip.use {outputStream->
//            outputStream.write(this.toByteArray())
//        }
//        val byteArray = byteArrayStream.toByteArray()
//        byteArrayStream.close()
//        byteArray
        Base64.encode(this.toByteArray(), Base64.DEFAULT)
    } catch (ex: Exception) {
        byteArrayOf()
    }
}

//No Need For HuffMan Because Base64 retains formatting
fun ByteArray.toCustomString(): String {
    return try {
//        val bis = ByteArrayInputStream(this)
//        val gis = GZIPInputStream(bis)
//        val br = BufferedReader(InputStreamReader(gis, "UTF-8"))
//        val sb = StringBuilder()
//        var line: String?
//        while (br.readLine().also { line = it } != null) {
//            sb.append(line)
//        }
//        br.close()
//        gis.close()
//        bis.close()
        val sb = String(Base64.decode(this, Base64.DEFAULT))
        val output = sb.transformTextToHtml()
        //Log.e("sb", output)
        output
    } catch (ex: Exception) {
        ""
    }
}

fun String.transformTextToHtml(): String {

    val regexCodeBlock = """(`{3}[\s\S]*?`{3})""".toRegex()
    val regexWholeSpaceLine = """(^|\n)\s*\n""".toRegex()
    var formattedText = this.replace(regexCodeBlock) { matchResult ->
        val codeBlock = matchResult.value.removePrefix("```").removeSuffix("```").trim()
        "<$PRE_TAG>${
            codeBlock.replace("\n", "<br>")
                .replace(" ", "&nbsp;")
        }</$PRE_TAG>"
    }
    formattedText = formattedText.replace(regexWholeSpaceLine) { matchResult ->
        "<br><br>"
    }
    return formattedText
}

fun String.revertHtmlToPlainText(): String {

    val regexPreTag = """<$PRE_TAG>([\s\S]*?)</$PRE_TAG>""".toRegex()
    val regexBrTag = """<br>""".toRegex()
    val regexNbsp = """&nbsp;""".toRegex()

    val plainText: String = this.replace(regexPreTag) { matchResult ->
        val codeBlock =
            matchResult.groupValues[1].replace(regexBrTag, "\n")
                .replace(regexNbsp, " ")
        "```$codeBlock```"
    }.replace(Regex("<.*?>"), "")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&amp;", "&")
        .replace(regexBrTag, "\n")
        .replace("```", "")
    //.replace(regexNbsp, " ")

    return plainText
}


fun String.dividePlainHtmlAndCode(): List<Pair<String, Boolean>> {

    val result = ArrayList<Pair<String, Boolean>>()
    var currentIndex = 0
    val preTag = "<$PRE_TAG>"
    val preTagLength = preTag.length
    val postTag = "</$PRE_TAG>"
    val postTagLength = postTag.length

    while (currentIndex < this.length) {
        val startIndex = this.indexOf(preTag, currentIndex)
        if (startIndex == -1) {
            // No more <pre> tag found, add the remaining string to the result
            result.add(this.substring(currentIndex) to false)
            break
        }
        val endIndex = this.indexOf(postTag, startIndex)
        if (endIndex == -1) {
            // No corresponding </pre> tag found, add the remaining string to the result
            result.add(this.substring(currentIndex) to false)
            break
        }
        // Add the non-pre tag portion to the result
        if (startIndex > currentIndex) {
            result.add(this.substring(currentIndex, startIndex) to false)
        }
        // Add the pre tag portion to the result
        val preContent = this.substring(startIndex + preTagLength, endIndex).trim()
        result.add(preContent to true)
        currentIndex = endIndex + postTagLength
    }

    return result
}

fun List<Pair<String, Boolean>>.revertCodeOrPlainHtmlBackToHtml(): String {

    val stringBuilder = StringBuilder()
    for (pair in this) {
        if (pair.second) {
            // If the pair is enclosed in <pre> tag, append it as is
            stringBuilder.append("<$PRE_TAG>${pair.first}</$PRE_TAG>")
        } else {
            // If the pair is not enclosed in <pre> tag, escape any special characters and append it
            stringBuilder.append(
                pair.first.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;")
            )
        }
    }
    return stringBuilder.toString()
}

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun generateRandomId(): String {
    return (1..20)
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("");
}