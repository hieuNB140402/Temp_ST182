package com.audio.example.ui.record

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.audio.example.R
import com.audio.example.core.base.BaseActivity
import com.audio.example.core.extensions.checkPermissions
import com.audio.example.core.extensions.goToSettings
import com.audio.example.core.extensions.handleBackLeftToRight
import com.audio.example.core.extensions.hideNavigation
import com.audio.example.core.extensions.requestPermission
import com.audio.example.core.extensions.setOnSingleClickWithSound
import com.audio.example.core.extensions.visible
import com.audio.example.core.utils.key.PermissionKey
import com.audio.example.core.utils.key.RequestKey
import com.audio.example.databinding.ActivityRecordBinding
import com.audio.example.ui.permission.PermissionViewModel
import kotlin.getValue

class RecordActivity : BaseActivity<ActivityRecordBinding>() {
    private val permissionViewModel: PermissionViewModel by viewModels()

    private var settingsDialog: AlertDialog? = null

    override fun setViewBinding(): ActivityRecordBinding {
        return ActivityRecordBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        checkRecordAudioPermission()
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClickWithSound { handleBackLeftToRight() }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()
            tvCenter.text = getString(R.string.record)
            tvCenter.visible()
        }
    }

    private fun checkRecordAudioPermission() {
        if (!checkPermissions(permissionViewModel.getRecordAudioPermissions())) {
            if (permissionViewModel.needGoToSettings(sharePreference, PermissionKey.RECORD_AUDIO_KEY)) {
                goToSettings(settingsDialog = { dialog ->
                    settingsDialog = dialog as AlertDialog?
                }, onCancelClick = {
                    finish()
                })
            } else {
                requestPermission(permissionViewModel.getRecordAudioPermissions(), RequestKey.RECORD_AUDIO_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (requestCode) {
            RequestKey.RECORD_AUDIO_REQUEST_CODE -> permissionViewModel.updateRecordAudioGranted(
                sharePreference, granted
            )
        }
        if (!granted) {
            checkRecordAudioPermission()
        }
    }

    override fun onRestart() {
        super.onRestart()
//        settingsDialog?.let {
//            if (it.isShowing) {
//                it.dismiss()
//                hideNavigation(true)
//            }
//        }
        checkRecordAudioPermission()
    }
}