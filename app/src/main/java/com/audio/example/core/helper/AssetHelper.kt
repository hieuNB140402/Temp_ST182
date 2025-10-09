package com.audio.example.core.helper

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.audio.example.core.utils.key.AssetsKey
import com.audio.example.core.utils.key.ValueKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

object AssetHelper {
    // Read sub folder
    fun getSubfoldersAsset(context: Context, path: String): ArrayList<String> {
        val allData = context.assets.list(path)
        val sortedData = MediaHelper.sortAsset(allData)?.map { "${AssetsKey.ASSET_MANAGER}/$path/$it" }?.toCollection(ArrayList())
        return sortedData ?: arrayListOf()
    }

    // Read sub folder
    fun getSubfoldersNotDomainAsset(context: Context, path: String): ArrayList<String> {
        val allData = context.assets.list(path)
        val sortedData = MediaHelper.sortAsset(allData)?.map { "${AssetsKey.DATA}/$it" }?.toCollection(ArrayList())
        return sortedData ?: arrayListOf()
    }

    // Read file txt -> json -> T
    inline fun <reified T> readJsonAsset(context: Context, path: String): T? {
        return try {
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            Gson().fromJson(json, T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Read file txt -> json -> list
    inline fun <reified T> readTextToJsonAssets(context: Context, path: String): ArrayList<T> {
        return try {
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            val type = object : TypeToken<ArrayList<T>>() {}.type
            Gson().fromJson(json, type) ?: arrayListOf()
        } catch (e: Exception) {
            e.printStackTrace()
            arrayListOf()
        }
    }

    // Read file -> bitmap
    fun getBitmapFromAsset(context: Context, fileName: String): Bitmap? {
        return try {
            context.assets.open(fileName).use { input ->
                android.graphics.BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // file asset -> internal
    fun copyAssetToInternal(context: Context, fileName: String): File? {
        return try {
            val outFile = File(context.filesDir, fileName)
            outFile.parentFile?.mkdirs()

            if (!outFile.exists()) {
                context.assets.open(fileName).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            outFile
        } catch (e: Exception) {
            Log.e("nbhieu", "Copy asset failed: ${e.message}")
            null
        }
    }


    fun copyAssetToCache(context: Context, assetFileName: String): File? {
        return try {
            val fileNameWithoutDomain = if (assetFileName.startsWith("file:///android_asset/")) {
                assetFileName.removePrefix("file:///android_asset/")
            } else {
                assetFileName
            }

            val inputStream = context.assets.open(fileNameWithoutDomain)

            val outFile = File(context.cacheDir, fileNameWithoutDomain)

            outFile.parentFile?.mkdirs()

            val outputStream = FileOutputStream(outFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            outFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun copyAssetToExternal(context: Context, assetFileName: String): File? {
        return try {
            val inputStream = context.assets.open(assetFileName)

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val outFile = File(downloadsDir, assetFileName)

            val outputStream = FileOutputStream(outFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            outFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // ---------------------------------------------------------------------------------------------

}