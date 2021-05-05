package com.darrenyuen.webdavclient.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.darrenyuen.webdavclient.App
import java.security.AccessController.getContext

/**
 * Create by yuan on 2021/5/5
 */
object ClipboardUtil {
    fun copyToClipboard(text: String) {
        val clipboardManager = App.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
    }
}