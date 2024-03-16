package com.cjrodriguez.cjchatgpt.data.util

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View.MeasureSpec
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.ChatEntity
import com.cjrodriguez.cjchatgpt.data.datasource.cache.model.TopicEntity
import io.noties.markwon.Markwon
import io.noties.markwon.image.ImagesPlugin
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

const val GPT_3 = "GPT3"
const val CHAT_DB = "Chat.db"
const val GPT_VERSION_KEY = "gptVersionKey"
const val CHAT_SCREEN = "chatScreen"
const val TOPIC_SCREEN = "topicScreen"
const val GPT_SETTINGS = "settings"
const val PRE_TAG = "pre-wrap"
const val CHAT_KEY = "key"
const val SUCCESS = "success"
const val LOADING = "LOADING"
const val ERROR = "ERROR"
const val PASSWORD = "password"
const val SUMMARIZE_PROMPT =
    "Summarize this message to 5 words max and no special symbols just plain text"
const val SUMMARIZE_HISTORY_PROMPT = "Summarize this chat history as short as possible " +
        "and keep the key points so I can use for future chats"
const val CHAT_HISTORY_REFER_PROMPT =
    "Use this history to get about the previous chats on the same thread:"
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

fun ByteArrayOutputStream.toCustomString(): String {
    return try {
        val bytes = this.toByteArray()
        return String(Base64.decode(bytes, Base64.DEFAULT))
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
    textToAppend: String,
    topicId: String,
    lastCreatedIndex: Int,
    modelId: String,
    imageUrls: List<String> = listOf(),
    chatTopicDao: ChatTopicDao
) {
    val affectedRows =
        chatTopicDao.appendTextToContentMessage(messageId, textToAppend)

    if (affectedRows == 0) {
        chatTopicDao.insertChatResponse(
            ChatEntity(
                messageId = messageId,
                topicId = topicId,
                expandedContent = textToAppend,
                modelId = modelId,
                imageUrls = imageUrls,
                isUserGenerated = false,
                lastCreatedIndex = lastCreatedIndex + 2
            )
        )
    }
}

private fun convertImageUrlToByteArray(imageUrl: String): ByteArray {
    val url = URL(imageUrl)
    val inputStream = url.openStream()
    return org.apache.commons.io.IOUtils.toByteArray(inputStream)
}

fun ByteArray.toShortFileName(): String {
    val base64Encoded = Base64.encodeToString(this, Base64.DEFAULT)

    val safeFileName = base64Encoded
        .replace("/", "_")
        .replace("+", "-")
        .replace("=", "")

    return safeFileName.take(10)
}

fun storeImageInCache(
    imageUrl: String,
    isImageUrl: Boolean = true,
    topicId: String,
    context: Context
): String? {
    val byteArray = if (isImageUrl) {
        convertImageUrlToByteArray(imageUrl)
    } else {
        val markwon = Markwon.builder(context).usePlugin(ImagesPlugin.create()).build()
        val renderedMarkdown = markwon.toMarkdown(imageUrl)

        val textView = android.widget.TextView(context).apply {
            text = renderedMarkdown
            textSize = 18f
            setTextColor(android.graphics.Color.BLACK)
            setBackgroundColor(android.graphics.Color.WHITE)
            measure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
            )
            layout(0, 0, measuredWidth, measuredHeight)
        }

        val bitmap = Bitmap.createBitmap(
            textView.measuredWidth,
            textView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        textView.draw(canvas)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.toByteArray()
    }
    val imageTitle = if (isImageUrl) byteArray.toCustomString() else byteArray.toShortFileName()
    return storeByteArrayToTemp(topicId, context, imageTitle, byteArray)
}

fun storeInTempFolderAndReturnUrl(
    bitmap: Bitmap,
    topicId: String,
    context: Context,
): String? {
    ByteArrayOutputStream().use { stream ->
        bitmap.compress(JPEG, 100, stream)
        val byteArray = stream.toByteArray() ?: return null
        val customName = byteArray.toCustomString()
        val fileName = customName.ifEmpty { generateRandomId() }
        return storeByteArrayToTemp(
            topicId = topicId,
            context = context,
            imageTitle = fileName,
            byteArray = byteArray
        )
    }
}

fun storeByteArrayToTemp(
    topicId: String,
    context: Context,
    imageTitle: String,
    byteArray: ByteArray?
): String? {
    return try {
        val directoryPath = "images/$topicId"
        val imagesDir = File(context.getExternalFilesDir(directoryPath), "")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        context.contentResolver.openFileDescriptor(
            Uri
                .fromFile(
                    File(
                        context.getExternalFilesDir(directoryPath)?.absolutePath
                                + "/" + imageTitle + ".jpg"
                    )
                ), "w"
        )?.use {
            FileOutputStream(it.fileDescriptor).use {
                it.write(
                    byteArray
                )
            }
        }
        val imageFile = File(imagesDir, "$imageTitle.jpg")
        return Uri.fromFile(imageFile).toString()

    } catch (e: FileNotFoundException) {
        Log.e("file", e.toString())
        null
    } catch (e: IOException) {
        Log.e("io", e.toString())
        null
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

fun String.determineContentType(urlPath: String): String {
    return when {
        "image" in urlPath -> "Image"
        "document" in urlPath -> "Document"
        else -> "Unknown"
    }
}

fun createBitmapFromContentUri(context: Context, contentUri: String): Bitmap? {
    val uri = Uri.parse(contentUri)
    val bitmap = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        Log.e("gemini", e.toString())
        null
    }
    return bitmap
}

fun triggerHapticFeedback(context: Context) {
    val vibrator = if (VERSION.SDK_INT >= VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    if (VERSION.SDK_INT >= VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(50)
    }
}