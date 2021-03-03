package com.darrenyuen.webdavclient.util

import android.content.Context

/**
 * Create by yuan on 2021/3/3
 */
object LayoutUtil {

    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }
}