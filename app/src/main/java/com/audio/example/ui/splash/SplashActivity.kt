package com.audio.example.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.audio.example.core.base.BaseActivity
import com.audio.example.core.extensions.initNetworkMonitor
import com.audio.example.core.extensions.select
import com.audio.example.core.extensions.setBackgroundConnerSmooth
import com.audio.example.core.utils.DataLocal
import com.audio.example.core.utils.state.HandleState
import com.audio.example.databinding.ActivitySplashBinding
import com.audio.example.ui.intro.IntroActivity
import com.audio.example.ui.language.LanguageActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    var intentActivity: Intent? = null
    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action.equals(Intent.ACTION_MAIN)
        ) {
            finish()
            return
        }

        intentActivity = if (sharePreference.getIsFirstLang()) {
            Intent(this, LanguageActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }
        initNetworkMonitor()
        lifecycleScope.launch {
            delay(3000)
            startActivity(intentActivity)
            finishAffinity()
        }
    }

    override fun viewListener() {}

    override fun initActionBar() {}

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {}
}