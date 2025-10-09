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


fun Context.checkPermissions(listPermission: Array<String>): Boolean {
    return listPermission.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun Activity.requestPermission(permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissions, requestCode)
}

fun Activity.goToSettings() {
    LanguageHelper.setLocale(this)
    val dialog =
        AlertDialog.Builder(this).setTitle(R.string.go_to_setting_title).setMessage(R.string.go_to_setting_message)
            .setPositiveButton(R.string.settings) { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${this@goToSettings.packageName}".toUri()
                }
                this.startActivity(intent)
                dialog.dismiss()
                hideNavigation()
            }.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                hideNavigation()
            }.setCancelable(false).create()

    dialog.show()
    val positiveButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    val negativeButton: Button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
    positiveButton.setTextColor("#2CC3FF".toColorInt())
    negativeButton.setTextColor(getColor(R.color.black))
}
