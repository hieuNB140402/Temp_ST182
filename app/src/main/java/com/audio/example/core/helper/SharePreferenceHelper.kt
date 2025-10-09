package com.audio.example.core.helper

import android.content.Context
import android.content.SharedPreferences
import com.audio.example.core.utils.key.PermissionKey.CAMERA_KEY
import com.audio.example.core.utils.key.PermissionKey.NOTIFICATION_KEY
import com.audio.example.core.utils.key.PermissionKey.PITCH_SHIFT_KEY
import com.audio.example.core.utils.key.PermissionKey.QUANTITY_UNZIPPED
import com.audio.example.core.utils.key.PermissionKey.RECORD_AUDIO_KEY
import com.audio.example.core.utils.key.PermissionKey.SPEED_KEY
import com.audio.example.core.utils.key.PermissionKey.STORAGE_KEY
import com.audio.example.core.utils.key.SharePreferenceKey
import com.audio.example.core.utils.key.SharePreferenceKey.COUNT_BACK_KEY
import com.audio.example.core.utils.key.SharePreferenceKey.FIRST_LANG_KEY
import com.audio.example.core.utils.key.SharePreferenceKey.FIRST_PERMISSION_KEY
import com.audio.example.core.utils.key.SharePreferenceKey.KEY_LANGUAGE
import com.audio.example.core.utils.key.SharePreferenceKey.RATE_KEY
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharePreferenceHelper(val context: Context) {
    val preferences: SharedPreferences =
        context.getSharedPreferences(SharePreferenceKey.SHARE_KEY, Context.MODE_PRIVATE)

    // Language
    fun getPreLanguage(): String {
        return preferences.getString(KEY_LANGUAGE, "") ?: ""
    }

    fun setPreLanguage(language: String) {
        val editor = preferences.edit()
        editor.putString(KEY_LANGUAGE, language)
        editor.apply()
    }

    // First Language
    fun getIsFirstLang(): Boolean {
        return preferences.getBoolean(FIRST_LANG_KEY, true)
    }

    fun setIsFirstLang(isFirstAccess: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(FIRST_LANG_KEY, isFirstAccess)
        editor.apply()
    }

    // Permission
    fun getIsFirstPermission(): Boolean {
        return preferences.getBoolean(FIRST_PERMISSION_KEY, true)
    }

    fun setIsFirstPermission(isFirstAccess: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(FIRST_PERMISSION_KEY, isFirstAccess)
        editor.apply()
    }

    // Rate
    fun getIsRate(context: Context): Boolean {
        return preferences.getBoolean(RATE_KEY, false)
    }

    fun setIsRate(isFirstAccess: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(RATE_KEY, isFirstAccess)
        editor.apply()
    }

    // Back
    fun setCountBack(countBack: Int) {
        val editor = preferences.edit()
        editor.putInt(COUNT_BACK_KEY, countBack)
        editor.apply()
    }

    fun getCountBack(): Int {
        return preferences.getInt(COUNT_BACK_KEY, 0)
    }

    // Storage Permission
    fun getStoragePermission(): Int {
        return preferences.getInt(STORAGE_KEY, 0)
    }

    fun setStoragePermission(count: Int) {
        val editor = preferences.edit()
        editor.putInt(STORAGE_KEY, count)
        editor.apply()
    }

    // Notification Permission
    fun getNotificationPermission(): Int {
        return preferences.getInt(NOTIFICATION_KEY, 0)
    }

    fun setNotificationPermission(count: Int) {
        val editor = preferences.edit()
        editor.putInt(NOTIFICATION_KEY, count)
        editor.apply()
    }

    // Camera Permission
    fun getCameraPermission(): Int {
        return preferences.getInt(CAMERA_KEY, 0)
    }

    fun setCameraPermission(count: Int) {
        val editor = preferences.edit()
        editor.putInt(CAMERA_KEY, count)
        editor.apply()
    }

    // Record Audio Permission
    fun getRecordAudioPermission(): Int {
        return preferences.getInt(RECORD_AUDIO_KEY, 0)
    }

    fun setRecordAudioPermission(count: Int) {
        val editor = preferences.edit()
        editor.putInt(RECORD_AUDIO_KEY, count)
        editor.apply()
    }

    // Data asset
    fun getQuantityUnzipped(): MutableSet<Int> {
        val json = preferences.getString(QUANTITY_UNZIPPED, "[]")
        val type = object : TypeToken<MutableSet<Int>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun setQuantityUnzipped(count: MutableSet<Int>) {
        val editor = preferences.edit()
        val json = Gson().toJson(count)
        editor.putString(QUANTITY_UNZIPPED, json)
        editor.apply()
    }

    // Speed
    fun getSpeedAudio(): Float {
        return preferences.getFloat(SPEED_KEY, 1f)
    }

    fun setSpeedAudio(speed: Float) {
        val editor = preferences.edit()
        editor.putFloat(SPEED_KEY, speed)
        editor.apply()
    }

    // pitch shift
    fun getPitchShiftAudio(): Float {
        return preferences.getFloat(PITCH_SHIFT_KEY, 1f)
    }

    fun getPitchShiftAudio(pitchShift: Float) {
        val editor = preferences.edit()
        editor.putFloat(PITCH_SHIFT_KEY, pitchShift)
        editor.apply()
    }

}