package com.audio.example.core.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.audio.example.R
import com.audio.example.core.utils.key.ValueKey
import com.audio.example.core.utils.state.HandleState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL
import androidx.core.graphics.scale
import com.audio.example.core.utils.key.AssetsKey
import com.audio.example.core.utils.state.SaveState
import com.audio.example.data.local.Music
import com.audio.example.data.model.RecordModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import java.net.HttpURLConnection

object MediaHelper {
    // Sort file (folder)
    fun sortAsset(listFiles: Array<String>?): List<String>? {
        val sortedFiles = listFiles?.sortedWith(compareBy { fileName ->
            val matchResult = Regex("\\d+").find(fileName)
            matchResult?.value?.toIntOrNull() ?: Int.MAX_VALUE
        })
        return sortedFiles
    }

    // Get file from internal
    fun getFileInternal(context: Context, album: String, isMusic: Boolean): ArrayList<String> {
        val recordList = ArrayList<String>()
        val targetDir = File(context.filesDir, album)
        if (targetDir.exists() && targetDir.isDirectory) {
            targetDir.listFiles()?.filter { if (!isMusic) isImageFile(it) else isMusicFile(it) }
                ?.sortedByDescending { it.lastModified() }?.forEach { file ->

                    recordList.add(
                        file.absolutePath
                    )
                }
        }
        return recordList
    }

    // is file?
    fun isImageFile(file: File): Boolean {
        val imageExtensions = listOf("jpg", "jpeg", "png", "bmp", "webp")
        val extension = file.extension.lowercase()
        return file.isFile && imageExtensions.contains(extension)
    }

    fun isMusicFile(file: File): Boolean {
        val imageExtensions = listOf("wav")
        val extension = file.extension.lowercase()
        return file.isFile && imageExtensions.contains(extension)
    }

    private fun getAudioDuration(context: Context, file: File): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.fromFile(file))
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toIntOrNull() ?: 0
            retriever.release()
            duration
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun deleteFileByPath(pathList: ArrayList<String>): Flow<HandleState> = flow {
        emit(HandleState.LOADING)
        try {
            for (i in 0 until pathList.size) {
                val file = File(pathList[i])
                if (file.exists()) {
                    file.delete()
                }
            }
            emit(HandleState.SUCCESS)
        } catch (e: Exception) {
            emit(HandleState.FAIL)
        }
    }.flowOn(Dispatchers.IO)


    @SuppressLint("Recycle")
    fun saveAudioToDownload(context: Context, internalPath: String, displayName: String): Boolean {
        return try {
            val inputFile = File(internalPath)
            if (!inputFile.exists()) return false

            val inputStream = inputFile.inputStream()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (API 29+) - Scoped Storage
                val resolver = context.contentResolver
                val name = displayName.substringBeforeLast(".")
                val ext = displayName.substringAfterLast(".", "wav")

                // Kiểm tra trùng tên
                var finalName = displayName
                var index = 1
                while (true) {
                    val existing = resolver.query(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                        "${MediaStore.MediaColumns.DISPLAY_NAME}=?",
                        arrayOf(finalName),
                        null
                    )
                    val exists = existing?.use { it.moveToFirst() } == true
                    if (!exists) break
                    finalName = "$name($index).$ext"
                    index++
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, finalName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/${ValueKey.DOWNLOAD_ALBUM}"
                    )
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return false

                resolver.openOutputStream(uri)?.use { output ->
                    inputStream.copyTo(output)
                }
            } else {
                // Android 9 trở xuống
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, ValueKey.DOWNLOAD_ALBUM)
                if (!appDir.exists()) appDir.mkdirs()

                var finalFile = File(appDir, displayName)
                var index = 1
                val name = displayName.substringBeforeLast(".")
                val ext = displayName.substringAfterLast(".", "wav")

                while (finalFile.exists()) {
                    finalFile = File(appDir, "$name($index).$ext")
                    index++
                }

                FileOutputStream(finalFile).use { output ->
                    inputStream.copyTo(output)
                }
            }
            inputStream.close()
            true
        } catch (e: Exception) {
            Log.e("nbhieu", "Download Error: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    suspend fun downloadVideoCompat(context: Context, videoUrl: String): HandleState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadUsingMediaStore(context, videoUrl)
        } else {
            downloadUsingDownloadManager(context, videoUrl)
        }
    }

    private suspend fun downloadUsingMediaStore(
        context: Context, videoUrl: String
    ): HandleState {
        return try {
            val resolver = context.contentResolver
            val fileName = StringHelper.generateRandomVideoFileName()
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES + "/" + ValueKey.DOWNLOAD_ALBUM
                )
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            val videoUri =
                resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return HandleState.FAIL

            URL(videoUrl).openStream().use { input ->
                resolver.openOutputStream(videoUri)?.use { output ->
                    input.copyTo(output)
                } ?: return HandleState.FAIL
            }

            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(videoUri, contentValues, null, null)
            HandleState.SUCCESS
        } catch (e: Exception) {
            Log.e("nbhieu", "downloadUsingMediaStore: ${e.message}")
            HandleState.FAIL
        }
    }


    private suspend fun downloadUsingDownloadManager(
        context: Context, videoUrl: String
    ): HandleState {
        return try {
            val fileName = StringHelper.generateRandomVideoFileName()
            val request = DownloadManager.Request(videoUrl.toUri()).apply {
                setTitle(context.getString(R.string.downloading))
                setDescription(fileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_MOVIES, "/${ValueKey.DOWNLOAD_ALBUM}/$fileName"
                )
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            HandleState.SUCCESS
        } catch (e: Exception) {
            HandleState.FAIL
        }
    }

    inline fun <reified T> writeListToFile(context: Context, fileName: String, list: List<T>) {
        try {
            val json = Gson().toJson(list)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inline fun <reified T> readListFromFile(context: Context, fileName: String): List<T> {
        return try {
            val json = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<T>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: FileNotFoundException) {
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun checkFileInternal(context: Context, fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists() || file.length() > 0
    }

    suspend fun downloadVideoToCache(context: Context, videoUrl: String): File? =
        withContext(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir


                cacheDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.endsWith(".mp4")) {
                        file.delete()
                    }
                }

                val fileName = "wallpaper_${System.currentTimeMillis()}.mp4"
                val file = File(cacheDir, fileName)

                val url = URL(videoUrl)
                url.openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun Activity.saveVideoToInternalStorage(album: String, videoUrl: String): String? {
        val fileName = StringHelper.generateRandomVideoFileName()

        return try {
            val directory = File(filesDir, album)
            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File(directory, fileName)

            val url = URL(videoUrl)
            url.openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun Activity.saveBitmapToCache(bitmap: Bitmap): File {
        val cachePath = File(cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "shared_image.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    fun saveBitmapToInternalStorage(
        context: Context, album: String, bitmap: Bitmap
    ): Flow<SaveState> = flow {
        emit(SaveState.Loading)

        try {
            val name = StringHelper.generateRandomImageFileName()
            val directory = File(context.filesDir, album)

            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File(directory, name)

            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                output.flush()
            }

            bitmap.recycle()

            emit(SaveState.Success(file.absolutePath))
        } catch (e: Exception) {
            emit(SaveState.Error(e))
        }
    }.flowOn(Dispatchers.IO)


    fun Activity.saveBitmapToInternalStorage(
        album: String, bitmap: Bitmap, nameInput: Int
    ): String? {
        val name = "$nameInput.png"

        return try {
            val directory = File(filesDir, album)
            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File(directory, name)

            val fileOutputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

            fileOutputStream.flush()
            fileOutputStream.close()

            bitmap.recycle()
            file.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun Activity.saveBitmapToInternalStorageZip(bitmap: Bitmap): String? {
        val name = StringHelper.generateRandomImageFileName()
        // Giảm kích thước ảnh xuống 512x512 px
        val resizedBitmap = bitmap.scale(512, 512)

        return try {
            val directory = File(filesDir, ValueKey.DOWNLOAD_ALBUM)

            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File(directory, "$name.png")

            val fileOutputStream = FileOutputStream(file)

            var quality = 100
            do {
                fileOutputStream.flush()
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutputStream)
                quality -= 5 // Giảm chất lượng sau mỗi lần nén
            } while (file.length() > 512 * 1024 && quality > 5) // 512 KB và chất lượng không dưới 5%

            fileOutputStream.flush()
            fileOutputStream.close()

            resizedBitmap.recycle()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downloadPartsToExternal(activity: Activity, pathList: List<String>): Flow<HandleState> =
        flow {
            emit(HandleState.LOADING)

            if (pathList.isEmpty()) {
                emit(HandleState.FAIL)
                return@flow
            }

            val bitmapList = BitmapHelper.convertPathsToBitmaps(activity, pathList)

            if (bitmapList.size == 1) {
                emitAll(saveBitmapToExternal(activity, bitmapList.first()))
            } else {
                var allSuccess = true
                for (bitmap in bitmapList) {
                    val state = saveBitmapToExternal(activity, bitmap).last()
                    if (state == HandleState.FAIL) {
                        allSuccess = false
                        break
                    }
                }
                emit(if (allSuccess) HandleState.SUCCESS else HandleState.FAIL)
            }
        }

    // bitmap -> external storage
    fun saveBitmapToExternal(activity: Activity, bitmap: Bitmap): Flow<HandleState> = flow {
        emit(HandleState.LOADING)

        val state = withContext(Dispatchers.IO) {
            try {
                val resolver = activity.contentResolver
                val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val contentValues = ContentValues().apply {
                    put(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        "image_${System.currentTimeMillis()}.png"
                    )
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "Pictures/${ValueKey.DOWNLOAD_ALBUM}"
                        )
                    } else {
                        val directory = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            ValueKey.DOWNLOAD_ALBUM
                        )
                        if (!directory.exists()) {
                            directory.mkdirs()
                        }
                        val filePath =
                            File(directory, "image_${System.currentTimeMillis()}.png").absolutePath
                        put(MediaStore.Images.Media.DATA, filePath)
                    }
                }

                val imageUri = resolver.insert(imageCollection, contentValues)
                    ?: return@withContext HandleState.FAIL

                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    val isSaved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    if (isSaved) HandleState.SUCCESS else HandleState.FAIL
                } ?: HandleState.FAIL

            } catch (e: Exception) {
                e.printStackTrace()
                HandleState.FAIL
            }
        }

        emit(state)
    }

    // get image external storage
    @SuppressLint("Recycle")
    fun getAllImages(context: Context): List<Uri> {
        val images = mutableListOf<Uri>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                images.add(contentUri)
            }
        }

        return images
    }

    @SuppressLint("Range")
    fun downloadAssetVideo(context: Context, assetFileName: String): Flow<HandleState> = flow {
        emit(HandleState.LOADING)
        val fileNameWithoutDomain = if (assetFileName.startsWith("file:///android_asset/")) {
            assetFileName.removePrefix("file:///android_asset/")
        } else {
            assetFileName
        }
        try {
            val inputStream = context.assets.open(fileNameWithoutDomain)
            val mimeType = "video/mp4"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileNameWithoutDomain)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/" + ValueKey.DOWNLOAD_ALBUM
                    )
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val collection =
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, values)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)

                    emit(HandleState.SUCCESS)
                }

            } else {
                // Android 9 trở xuống
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + ValueKey.DOWNLOAD_ALBUM)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val outFile = File(downloadsDir, fileNameWithoutDomain)
                val outputStream = FileOutputStream(outFile)
                inputStream.copyTo(outputStream)
                outputStream.close()

                // Cập nhật MediaStore để thấy ngay trong thư viện
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DATA, outFile.absolutePath)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                }
                context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

                emit(HandleState.SUCCESS)
            }

            inputStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
            emit(HandleState.FAIL)
        }
    }

    @SuppressLint("Range")
    fun downloadVideoURL(context: Context, url: String, fileName: String): Flow<HandleState> =
        flow {
            emit(HandleState.LOADING)
            val mimeType = "video/mp4"

            // Mở kết nối tới URL
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.doInput = true
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP error: ${connection.responseCode}")
            }

            val inputStream = connection.inputStream

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/" + ValueKey.DOWNLOAD_ALBUM
                    )
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val collection =
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, values)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)

                    emit(HandleState.SUCCESS)
                } ?: throw Exception("Insert to MediaStore failed")

            } else {
                // Android 9 trở xuống
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS + "/" + ValueKey.DOWNLOAD_ALBUM
                )
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val outFile = File(downloadsDir, fileName)
                FileOutputStream(outFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DATA, outFile.absolutePath)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                }
                context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

                emit(HandleState.SUCCESS)
            }

            inputStream.close()
            connection.disconnect()
        }.catch { e ->
            e.printStackTrace()
            emit(HandleState.FAIL)
        }



}