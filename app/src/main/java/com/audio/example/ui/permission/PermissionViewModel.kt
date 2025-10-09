package com.audio.example.ui.permission

import androidx.lifecycle.ViewModel
import com.audio.example.core.helper.PermissionHelper
import com.audio.example.core.helper.SharePreferenceHelper
import com.audio.example.core.utils.key.PermissionKey
import com.audio.example.core.utils.key.ValueKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PermissionViewModel : ViewModel() {

    private val _storageGranted = MutableStateFlow(false)
    val storageGranted: StateFlow<Boolean> = _storageGranted

    private val _notificationGranted = MutableStateFlow(false)
    val notificationGranted: StateFlow<Boolean> = _notificationGranted

    private val _recordAudioGranted = MutableStateFlow(false)
    val recordAudioGranted: StateFlow<Boolean> = _recordAudioGranted

    fun updateStorageGranted(sharePrefer: SharePreferenceHelper, granted: Boolean) {
        _storageGranted.value = granted
        sharePrefer.setStoragePermission(if (granted) 0 else sharePrefer.getStoragePermission() + 1)
    }

    fun updateNotificationGranted(sharePrefer: SharePreferenceHelper, granted: Boolean) {
        _notificationGranted.value = granted
        sharePrefer.setNotificationPermission(if (granted) 0 else sharePrefer.getNotificationPermission() + 1)
    }

    fun updateRecordAudioGranted(sharePrefer: SharePreferenceHelper, granted: Boolean) {
        _recordAudioGranted.value = granted
        sharePrefer.setRecordAudioPermission(if (granted) 0 else sharePrefer.getRecordAudioPermission() + 1)
    }

    fun needGoToSettings(sharePrefer: SharePreferenceHelper, permission: String): Boolean {
        return when (permission) {
            PermissionKey.STORAGE_KEY -> {
                sharePrefer.getStoragePermission() >= 2 && !_storageGranted.value
            }

            PermissionKey.NOTIFICATION_KEY -> {
                sharePrefer.getStoragePermission() >= 2 && !_notificationGranted.value
            }

            else -> {
                sharePrefer.getRecordAudioPermission() >= 2 && !_recordAudioGranted.value
            }
        }
    }

    fun getStoragePermissions() = PermissionHelper.storagePermission
    fun getNotificationPermissions() = PermissionHelper.notificationPermission
    fun getRecordAudioPermissions() = PermissionHelper.recordAudioPermission
}