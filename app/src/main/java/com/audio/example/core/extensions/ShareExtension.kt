package com.audio.example.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.audio.example.core.helper.AssetHelper
import com.audio.example.core.utils.state.HandleState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

fun Activity.shareImagesUris(imageUris: ArrayList<Uri>) {
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "*/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(intent, "Share Images"))
}

fun Activity.shareImagesPaths(imagePaths: ArrayList<String>) {
    val imageUris = ArrayList<Uri>()
    for (filePath in imagePaths) {
        val imageFile = File(filePath)
        val imageUri = FileProvider.getUriForFile(
            this, "${packageName}.provider", imageFile
        )
        imageUris.add(imageUri)
    }
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "*/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(intent, "Share Images"))
}

fun Activity.shareVideoUrl(videoUrl: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Check out this video")
        putExtra(Intent.EXTRA_TEXT, videoUrl)
    }
    startActivity(Intent.createChooser(intent, "Share video via"))
}
fun shareAssetVideo(context: Context, assetFileName: String): Flow<HandleState> = flow {
    emit(HandleState.LOADING)

    val cacheFile = AssetHelper.copyAssetToCache(context, assetFileName) ?: return@flow emit(HandleState.FAIL)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        cacheFile
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "video/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    withContext(Dispatchers.Main) {
        context.startActivity(Intent.createChooser(shareIntent, "Share video via"))
    }
    emit(HandleState.SUCCESS)
}
