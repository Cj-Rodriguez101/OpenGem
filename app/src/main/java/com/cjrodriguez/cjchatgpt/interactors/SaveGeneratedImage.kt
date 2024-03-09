package com.cjrodriguez.cjchatgpt.interactors

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Environment
import android.provider.MediaStore.Images.Media
import android.provider.MediaStore.MediaColumns
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.R.string
import com.cjrodriguez.cjchatgpt.data.util.generateRandomId
import com.cjrodriguez.cjchatgpt.presentation.BaseApplication
import com.cjrodriguez.cjchatgpt.presentation.util.DataState
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Objects
import javax.inject.Inject

class SaveGeneratedImage @Inject constructor(
    val context: BaseApplication
) {

    fun execute(imagePath: String): Flow<DataState<String>> = flow {
        try {
            emit(DataState.loading())
            val uri = Uri.parse(imagePath)
            val filePath = uri.path
            val fos: OutputStream?
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                val resolver: ContentResolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaColumns.DISPLAY_NAME, generateRandomId() + ".jpg")
                contentValues.put(MediaColumns.MIME_TYPE, "image/jpg")
                contentValues.put(MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                val imageUri = resolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = resolver.openOutputStream(Objects.requireNonNull<Uri?>(imageUri))
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, generateRandomId() + ".jpg")
                fos = FileOutputStream(image)
                saveToGallery(context, image)
            }
            val bitmap: Bitmap? = BitmapFactory.decodeFile(filePath)
            fos?.use {
                bitmap?.compress(JPEG, 100, fos)
            }
            emit(
                DataState.data(
                    data = "Success", message = GenericMessageInfo.Builder()
                        .id("SaveGeneratedImage.Success")
                        .title(context.getString(string.successfully_saved_picture))
                        .uiComponentType(UIComponentType.SnackBar)
                )
            )
        } catch (ex: Exception) {
            DataState.error<String>(
                message = GenericMessageInfo
                    .Builder().id("SaveGeneratedImage.Error")
                    .title(context.getString(R.string.error))
                    .description(context.getString(string.failed_to_save_image))
                    .uiComponentType(UIComponentType.Dialog)
            )
        }
    }

    private fun saveToGallery(context: Context, file: File) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.toString()),
            null, null
        )
    }
}