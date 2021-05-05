package com.darrenyuen.webdavclient

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.multidex.MultiDex

/**
 * Create by yuan on 2021/1/3
 */
class App : Application() {
    private val TAG = "SyllabusKt"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Application onCreate")
        context = applicationContext
        MultiDex.install(this)
    }

    @SuppressLint("StaticFieldLeak")
    companion object {
        var context: Context? = null
    }
}