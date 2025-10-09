package com.audio.example.ui.permission

import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audio.example.R
import com.audio.example.core.base.BaseActivity
import com.audio.example.core.extensions.checkPermissions
import com.audio.example.core.extensions.goToSettings
import com.audio.example.core.extensions.gone
import com.audio.example.core.extensions.requestPermission
import com.audio.example.core.extensions.select
import com.audio.example.core.extensions.setBackgroundConnerSmooth
import com.audio.example.core.extensions.setOnSingleClickWithSound
import com.audio.example.core.extensions.showToast
import com.audio.example.core.extensions.startIntentRightToLeft
import com.audio.example.core.extensions.visible
import com.audio.example.core.helper.StringHelper
import com.audio.example.core.utils.key.PermissionKey
import com.audio.example.core.utils.key.RequestKey
import com.audio.example.databinding.ActivityPermissionBinding
import com.audio.example.ui.home.HomeActivity
import kotlinx.coroutines.launch

class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {

    private val viewModel: PermissionViewModel by viewModels()

    override fun setViewBinding() = ActivityPermissionBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.btnStorage.visible()
            binding.btnNotification.gone()
        } else {
            binding.btnNotification.visible()
            binding.btnStorage.gone()
        }
    }

    override fun initText() {
        binding.actionBar.tvCenter.select()
        val textRes =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) R.string.to_access_13 else R.string.to_access

        binding.txtPer.text = TextUtils.concat(
            createColoredText(R.string.allow, R.color.white),
            " ",
            createColoredText(R.string.app_name, R.color.blue_2C, R.font.roboto_bold),
            " ",
            createColoredText(textRes, R.color.white)
        )
    }

    override fun viewListener() {
        binding.swPermission.setOnSingleClickWithSound {
            handlePermissionRequest(PermissionKey.STORAGE_KEY)
        }
        binding.swNotification.setOnSingleClickWithSound {
            handlePermissionRequest(PermissionKey.NOTIFICATION_KEY)
        }
        binding.swNotification.setOnSingleClickWithSound {
            handlePermissionRequest(PermissionKey.RECORD_AUDIO_KEY)
        }
        binding.tvContinue.setOnSingleClickWithSound(1500) {
            handleContinue()
        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.storageGranted.collect { granted ->
                    updatePermissionUI(granted, true)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notificationGranted.collect { granted ->
                    updatePermissionUI(granted, false)
                }
            }
        }
    }

    private fun handlePermissionRequest(permission: String) {
        val perms = when (permission) {
            PermissionKey.STORAGE_KEY -> {
                viewModel.getStoragePermissions()
            }

            PermissionKey.NOTIFICATION_KEY -> {
                viewModel.getNotificationPermissions()
            }

            else -> viewModel.getRecordAudioPermissions()
        }
        if (checkPermissions(perms)) {
            showToast(
                when (permission) {
                    PermissionKey.STORAGE_KEY -> {
                        R.string.granted_storage
                    }

                    PermissionKey.NOTIFICATION_KEY -> {
                        R.string.granted_notification
                    }

                    else -> R.string.granted_record_audio
                }
            )
        } else if (viewModel.needGoToSettings(sharePreference, permission)) {
            goToSettings()
        } else {
            val requestCode = when (permission) {
                PermissionKey.STORAGE_KEY -> {
                    RequestKey.STORAGE_PERMISSION_CODE
                }

                PermissionKey.NOTIFICATION_KEY -> {
                    RequestKey.NOTIFICATION_PERMISSION_CODE
                }

                else -> RequestKey.RECORD_AUDIO_REQUEST_CODE
            }
            requestPermission(perms, requestCode)
        }
    }

    private fun updatePermissionUI(granted: Boolean, isStorage: Boolean) {
        val imageView = if (isStorage) binding.swPermission else binding.swNotification
        imageView.setImageResource(if (granted) R.drawable.ic_sw_on else R.drawable.ic_sw_off)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (requestCode) {
            RequestKey.STORAGE_PERMISSION_CODE -> viewModel.updateStorageGranted(sharePreference, granted)
            RequestKey.NOTIFICATION_PERMISSION_CODE -> viewModel.updateNotificationGranted(sharePreference, granted)
            RequestKey.RECORD_AUDIO_REQUEST_CODE -> viewModel.updateRecordAudioGranted(sharePreference, granted)
        }
        if (granted) {
            showToast(
                when (requestCode) {
                    RequestKey.STORAGE_PERMISSION_CODE -> {
                        R.string.granted_storage
                    }

                    RequestKey.NOTIFICATION_PERMISSION_CODE -> {
                        R.string.granted_notification
                    }

                    else -> R.string.granted_record_audio
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateStorageGranted(
            sharePreference, checkPermissions(viewModel.getStoragePermissions())
        )
        viewModel.updateNotificationGranted(
            sharePreference, checkPermissions(viewModel.getNotificationPermissions())
        )
        viewModel.updateRecordAudioGranted(
            sharePreference, checkPermissions(viewModel.getRecordAudioPermissions())
        )
    }


    override fun initActionBar() {
        binding.actionBar.tvStart.apply {
            text = getString(R.string.permission)
            visible()
        }
    }

    private fun createColoredText(
        @androidx.annotation.StringRes textRes: Int,
        @androidx.annotation.ColorRes colorRes: Int,
        font: Int = R.font.roboto_regular
    ) = StringHelper.changeColor(this, getString(textRes), colorRes, font)

    private fun handleContinue() {
        sharePreference.setIsFirstPermission(false)
        startIntentRightToLeft(HomeActivity::class.java)
        finishAffinity()
    }
}