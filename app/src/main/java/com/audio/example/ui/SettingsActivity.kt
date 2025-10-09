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
import com.audio.example.core.utils.key.IntentKey
import com.audio.example.core.utils.state.RateState
import com.audio.example.databinding.ActivitySettingsBinding
import com.audio.example.ui.language.LanguageActivity
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRate()
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClickWithSound { handleBackLeftToRight() }
            btnLang.setOnSingleClickWithSound { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShare.setOnSingleClickWithSound(1500) { shareApp() }
            btnRate.setOnSingleClickWithSound {
                rateApp(sharePreference) { state ->
                    if (state != RateState.CANCEL) {
                        binding.btnRate.gone()
                    }
                }
            }
            btnPolicy.setOnSingleClickWithSound(1500) { policy() }
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
}