package com.audio.example.ui.home

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.audio.example.R
import com.audio.example.core.base.BaseActivity
import com.audio.example.core.extensions.checkPermissions
import com.audio.example.core.extensions.goToSettings
import com.audio.example.core.extensions.rateApp
import com.audio.example.core.extensions.requestPermission
import com.audio.example.core.extensions.select
import com.audio.example.core.extensions.setOnSingleClickWithSound
import com.audio.example.core.extensions.showToast
import com.audio.example.core.extensions.startIntentRightToLeft
import com.audio.example.core.extensions.visible
import com.audio.example.core.helper.LanguageHelper
import com.audio.example.core.utils.key.PermissionKey
import com.audio.example.core.utils.key.RequestKey
import com.audio.example.core.utils.state.RateState
import com.audio.example.databinding.ActivityHomeBinding
import com.audio.example.ui.SettingsActivity
import com.audio.example.ui.my_record.MyRecordActivity
import com.audio.example.ui.permission.PermissionViewModel
import com.audio.example.ui.record.RecordActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
    }

    override fun dataObservable() {

    }

    override fun viewListener() {
        binding.apply {
            btnSettings.setOnSingleClickWithSound { startIntentRightToLeft(SettingsActivity::class.java) }
            btnRecord.setOnSingleClickWithSound { checkRecordAudioPermission() }
            btnMyRecord.setOnSingleClickWithSound { startIntentRightToLeft(MyRecordActivity::class.java) }
        }
    }

    override fun initText() {
        super.initText()
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            tvCenter.text = getString(R.string.reverse_voice)
            tvCenter.visible()
        }
    }

    private fun initRcv() {}

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!sharePreference.getIsRate(this) && sharePreference.getCountBack() % 2 == 0) {
            rateApp(sharePreference) { state ->
                when (state) {
                    RateState.LESS3 -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(1000)
                            exitProcess(0)
                        }
                    }

                    RateState.GREATER3 -> {}
                    RateState.CANCEL -> {
                        lifecycleScope.launch {
                            sharePreference.setCountBack(sharePreference.getCountBack() + 1)
                            withContext(Dispatchers.Main) {
                                delay(1000)
                                exitProcess(0)
                            }
                        }
                    }
                }
            }
        } else {
            exitProcess(0)
        }
    }

    private fun updateText() {
        binding.apply {
            actionBar.tvCenter.text = getString(R.string.reverse_voice)
            tvRecord.text = getString(R.string.record)
            tvMyRecord.text = getString(R.string.my_record)
            tvSettings.text = getString(R.string.settings)
        }
    }

    private fun checkRecordAudioPermission() {
        if (checkPermissions(permissionViewModel.getRecordAudioPermissions())) {
            startRecordActivity()
        } else if (permissionViewModel.needGoToSettings(
                sharePreference,
                PermissionKey.RECORD_AUDIO_KEY
            )
        ) {
            goToSettings()
        } else {
            requestPermission(
                permissionViewModel.getRecordAudioPermissions(),
                RequestKey.RECORD_AUDIO_REQUEST_CODE
            )
        }
    }

    private fun startRecordActivity() = startIntentRightToLeft(RecordActivity::class.java)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted =
            grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (requestCode) {
            RequestKey.RECORD_AUDIO_REQUEST_CODE -> permissionViewModel.updateRecordAudioGranted(
                sharePreference,
                granted
            )
        }
        if (granted) {
            startRecordActivity()
        }
    }

    override fun onRestart() {
        super.onRestart()
        LanguageHelper.setLocale(this)
        updateText()
    }
}