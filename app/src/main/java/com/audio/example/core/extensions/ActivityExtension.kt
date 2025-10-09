package com.audio.example.core.extensions

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import com.audio.example.core.helper.RateHelper
import com.audio.example.core.helper.SharePreferenceHelper
import com.audio.example.core.utils.state.RateState

fun Activity.shareApp() {
    ShareCompat.IntentBuilder.from(this).setType("text/plain").setChooserTitle("Chooser title")
        .setText("http://play.google.com/store/apps/details?id=" + (this).packageName)
        .startChooser()
}

fun Activity.policy() {
    val url = "https://sites.google.com/view/finger-play-game/"
    val i = Intent(Intent.ACTION_VIEW)
    i.data = url.toUri()
    startActivity(i)
}
fun Activity.rateApp(
    sharePreference: SharePreferenceHelper,
    onRateResult: (RateState) -> Unit = {}
) {
    RateHelper.showRateDialog(this, sharePreference, onRateResult)
}
fun isActivityInBackStack(context: Context, activityClass: Class<*>): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val tasks = activityManager.appTasks

    for (task in tasks) {
        val taskInfo = task.taskInfo
        val base = taskInfo.baseActivity?.className
        val top = taskInfo.topActivity?.className
        if (taskInfo.baseActivity?.className == activityClass.name || taskInfo.topActivity?.className == activityClass.name) {
            return true
        }
    }
    return false
}