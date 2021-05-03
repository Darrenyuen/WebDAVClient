package com.darrenyuen.webdavclient.util

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log

/**
 * Create by yuan on 2021/5/3
 */
object NetworkUtil {
    const val TAG = "NetworkUtil"
    fun isNetworkAvailable(activity: Activity): Boolean {
        val connectivityManager = activity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        connectivityManager.allNetworks.forEach {
//            if (it.)
//        }
        connectivityManager.allNetworkInfo.forEach {
            Log.i(TAG, "网络状态： ${it.state.toString()}")
            Log.i(TAG, "网络类型： ${it.typeName}")
            if (it.state == NetworkInfo.State.CONNECTED) return true;
        }
        return false;
    }
}