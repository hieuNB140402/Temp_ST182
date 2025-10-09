package com.audio.example.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.AssetDataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import com.audio.example.core.utils.key.RequestKey
import kotlin.collections.forEach


fun AppCompatActivity.deleteTempDataFolder(folder: String) {
    lifecycleScope.launch(Dispatchers.IO) {
        val targetDir = File(filesDir, folder)
        if (targetDir.exists() && targetDir.isDirectory) {
            val listFile = targetDir.listFiles()?.toCollection(ArrayList())
            listFile?.let {
                listFile.forEach {
                    it.delete()
                }
            }
        }
//        val dataTemp = getImageInternal(context, folder)
//        if (dataTemp.isNotEmpty()) {
//            dataTemp.forEach {
//                val file = File(it)
//                file.delete()
//            }
//        }
    }
}

fun Activity.openImagePicker() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, RequestKey.PICK_IMAGE_REQUEST_CODE)
}

@OptIn(UnstableApi::class)
fun playAssetVideo(context: Context, fileName: String, player: ExoPlayer) {
    try {
        // Uri theo chuẩn "asset:///" + fileName
        val uri = fileName.toUri()

        // Tạo DataSpec cho AssetDataSource
        val dataSpec = DataSpec(uri)

        // Tạo AssetDataSource
        val assetDataSource = AssetDataSource(context)
        assetDataSource.open(dataSpec)

        // Chuyển thành MediaItem
        val factory = { assetDataSource }
        val mediaItem = MediaItem.fromUri(uri)

        val mediaSource = ProgressiveMediaSource.Factory {
            assetDataSource
        }.createMediaSource(mediaItem)

        // Set vào player
        player.setMediaSource(mediaSource)
        player.prepare()
        player.repeatMode = ExoPlayer.REPEAT_MODE_ONE
//        player.playWhenReady = true
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun playSoundOnce(context: Context, fileName: String) {
    try {
        val afd = context.assets.openFd(fileName)
        val mediaPlayer = MediaPlayer()

        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()

        mediaPlayer.prepare()
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    } catch (e: Exception) {
        Log.e("nbhieu", "playSoundOnce: ${e.message}")
        e.printStackTrace()
    }
}


