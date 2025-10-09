package com.audio.example.core.helper

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.audio.example.R
import com.audio.example.core.extensions.showToast
import com.audio.example.core.utils.state.HandleState

object InternetHelper {
    fun checkInternet(context: Activity, state : (() -> Unit) = {}){
        if (isInternetAvailable(context)){
            state.invoke()
        }else{
            context.showToast(R.string.please_check_your_internet)
        }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}