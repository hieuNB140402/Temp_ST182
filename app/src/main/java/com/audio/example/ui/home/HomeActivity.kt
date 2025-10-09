package com.audio.example.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.audio.example.R
import com.audio.example.core.base.BaseActivity
import com.audio.example.core.extensions.rateApp
import com.audio.example.core.extensions.select
import com.audio.example.core.extensions.setOnSingleClickWithSound
import com.audio.example.core.extensions.startIntentRightToLeft
import com.audio.example.core.extensions.visible
import com.audio.example.core.helper.LanguageHelper
import com.audio.example.core.utils.state.RateState
import com.audio.example.databinding.ActivityHomeBinding
import com.audio.example.ui.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private val dataViewModel: DataViewModel by viewModels()

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
            actionBar.btnActionBarRight.setOnSingleClickWithSound { startIntentRightToLeft(SettingsActivity::class.java) }
        }
    }

    override fun initText() {
        super.initText()
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarRight.setImageResource(R.drawable.ic_settings)
            btnActionBarRight.visible()
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
//            stvFinger.text = getString(R.string.finger_play)
//            tvFinger.text = getString(R.string.finger_play)
//            stvFavorite.text = getString(R.string.favorite)
//            tvFavorite.text = getString(R.string.favorite)
//            tvHotViral.text = getString(R.string.hot_viral)
        }
    }



    override fun onRestart() {
        super.onRestart()
        LanguageHelper.setLocale(this)
        updateText()
    }
}