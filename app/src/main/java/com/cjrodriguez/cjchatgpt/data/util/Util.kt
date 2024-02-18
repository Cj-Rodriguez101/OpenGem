package com.cjrodriguez.cjchatgpt.data.util

import android.util.Base64
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity

const val GPT_3 = "GPT3"
const val CHAT_DB = "Chat.db"
const val GPT_VERSION_KEY = "gptVersionKey"
const val CHAT_SCREEN = "chatScreen"
const val TOPIC_SCREEN = "topicScreen"
const val GPT_SETTINGS = "settings"
const val PRE_TAG = "pre-wrap"
const val CHAT_KEY = "key"
const val SUCCESS = "success"
const val PASSWORD = "password"
const val SUMMARIZE_PROMPT = "Summarize this message to 5 words max"
const val SUMMARIZE_HISTORY_PROMPT = "Summarize this chat history as short as possible " +
        "and keep the key points so I can use for future chats"
const val CHAT_HISTORY_REFER_PROMPT = "Use this history to get more context on the based on the same thread:"
const val THE_REAL_PROMPT_IS = "Then the real prompt is"

fun String.toByteArrayCustom(): ByteArray {
    return try {
        Base64.encode(this.toByteArray(), Base64.DEFAULT)
    } catch (ex: Exception) {
        byteArrayOf()
    }
}

fun ByteArray.toCustomString(): String {
    return try {
        return String(Base64.decode(this, Base64.DEFAULT))
    } catch (ex: Exception) {
        ""
    }
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

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun generateRandomId(): String {
    return (1..20)
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

fun storeAndAppendTopic(
    topicId: String,
    it: String,
    chatTopicDao: ChatTopicDao
) {
    val affectedRows = chatTopicDao.appendTextToTopicTitle(
        topicId,
        it
    )

    if (affectedRows == 0) {
        chatTopicDao.insertTopic(
            TopicEntity(
                id = topicId, title = it
            )
        )
    }
}

fun storeAndAppendResponse(
    messageId: String,
    it: String,
    topicId: String,
    lastCreatedIndex: Int,
    modelId: String,
    chatTopicDao: ChatTopicDao
) {
    val affectedRows =
        chatTopicDao.appendTextToContentMessage(messageId, it)

    if (affectedRows == 0) {
        chatTopicDao.insertChatResponse(
            ChatEntity(
                messageId = messageId,
                topicId = topicId,
                expandedContent = it,
                modelId = modelId,
                isUserGenerated = false,
                lastCreatedIndex = lastCreatedIndex + 2
            )
        )
    }
}

suspend fun <T> getNewSummaryResponseFromModel(
    topicId: String,
    lastCreatedId: Int,
    chatTopicDao: ChatTopicDao,
    getSummaryResponse: suspend (List<ChatEntity>) -> T,
): T {
    val previousChatMessages =
        chatTopicDao.getAllChatsFromTopicStartingAfterIndex(topicId, lastCreatedId)
    return getSummaryResponse(previousChatMessages)
}