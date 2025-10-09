package com.audio.example.ui

import android.view.LayoutInflater
import com.audio.example.R
import com.audio.example.core.base.BaseActivity
import com.audio.example.core.extensions.gone
import com.audio.example.core.extensions.handleBackLeftToRight
import com.audio.example.core.extensions.policy
import com.audio.example.core.extensions.rateApp
import com.audio.example.core.extensions.select
import com.audio.example.core.extensions.setOnSingleClickWithSound
import com.audio.example.core.extensions.setTextContent
import com.audio.example.core.extensions.shareApp
import com.audio.example.core.extensions.startIntentRightToLeft
import com.audio.example.core.extensions.visible
import com.audio.example.core.utils.DataLocal
import com.audio.example.core.utils.key.IntentKey
import com.audio.example.core.utils.state.RateState
import com.audio.example.databinding.ActivitySettingsBinding
import com.audio.example.ui.language.LanguageActivity
import kotlin.jvm.java
import kotlin.math.abs

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRate()
        setSpeedStep(sharePreference.getSpeedAudio())
        setPitchStep(sharePreference.getPitchShiftAudio())
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClickWithSound { handleBackLeftToRight() }
            btnLang.setOnSingleClickWithSound {
                startIntentRightToLeft(
                    LanguageActivity::class.java, IntentKey.INTENT_KEY
                )
            }
            btnShare.setOnSingleClickWithSound(1500) { shareApp() }
            btnRate.setOnSingleClickWithSound {
                rateApp(sharePreference) { state ->
                    if (state != RateState.CANCEL) {
                        binding.btnRate.gone()
                    }
                }
            }
            btnPolicy.setOnSingleClickWithSound(1500) { policy() }

            spbSpeed.onStepChanged = { progressSpeed -> setUpSpeedShare(progressSpeed) }
            spbPitchShift.onStepChanged = { progressPitch -> setUpPitchShare(progressPitch) }
        }
    }

    override fun initText() {
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            tvCenter.setTextContent(this@SettingsActivity, R.string.settings)
            tvCenter.visible()

            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()
        }
    }

    private fun initRate() {
        if (sharePreference.getIsRate(this)) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }

    private fun setUpSpeedShare(step: Int) {
        val clamped = step.coerceIn(0, DataLocal.speedList.lastIndex)
        sharePreference.setSpeedAudio(DataLocal.speedList[clamped])
        binding.tvSpeed.text = "x${String.format("%.2f", DataLocal.speedList[clamped])}"
    }

    private fun setSpeedStep(speed: Float) {
        val progressSpeed = DataLocal.speedList.minByOrNull { abs(it - speed) }
            ?.let { DataLocal.speedList.indexOf(it) } ?: 3
        binding.spbSpeed.progress = progressSpeed
        setUpSpeedShare(progressSpeed)
    }


    private fun setUpPitchShare(step: Int) {
        val clamped = step.coerceIn(0, DataLocal.pitchList.lastIndex)
        val speed = DataLocal.pitchList[clamped]
        sharePreference.setPitchShiftAudio(speed)

        val display = clamped - 5
        binding.tvPitchShift.text = if (display > 0) "+$display" else "$display"
    }


    private fun setPitchStep(speed: Float) {
        val nearest = DataLocal.pitchList.minByOrNull { abs(it - speed) }
        val progressSpeed = nearest?.let { DataLocal.pitchList.indexOf(it) } ?: 5

        binding.spbPitchShift.progress = progressSpeed
        setUpPitchShare(progressSpeed)
    }

}