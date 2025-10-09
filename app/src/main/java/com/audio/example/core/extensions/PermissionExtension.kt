package com.audio.example.core.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.audio.example.R
import com.audio.example.core.helper.LanguageHelper
import com.audio.example.dialog.ConfirmDialog


fun Context.checkPermissions(listPermission: Array<String>): Boolean {
    return listPermission.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun Activity.requestPermission(permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissions, requestCode)
}

fun Activity.goToSettings(settingsDialog: ((ConfirmDialog) -> Unit)? = null, onCancelClick: (() -> Unit)? = null) {
    val confirmDialog = ConfirmDialog(this, R.string.go_to_setting_title, R.string.go_to_setting_message)
    LanguageHelper.setLocale(this)
    confirmDialog.show()
    settingsDialog?.invoke(confirmDialog)
    confirmDialog.onNoClick = {
        confirmDialog.dismiss()
        hideNavigation()
        onCancelClick?.invoke()
    }
    confirmDialog.onYesClick = {
        confirmDialog.dismiss()
        hideNavigation()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${this@goToSettings.packageName}".toUri()
        }
        startActivity(intent)
    }
}

