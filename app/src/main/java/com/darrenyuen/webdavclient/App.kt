package com.darrenyuen.webdavclient

import android.app.Application
import androidx.multidex.MultiDex

/**
 * Create by yuan on 2021/1/3
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }
}