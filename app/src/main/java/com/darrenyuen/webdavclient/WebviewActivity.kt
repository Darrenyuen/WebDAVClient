package com.darrenyuen.webdavclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView

class WebviewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var menuIcon: ImageView
    private lateinit var backIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
//        menuIcon = findViewById<ImageView>(R.id.menuIcon).apply { visibility = View.GONE }
//        backIcon = findViewById<ImageView>(R.id.backIcon).apply {
//            visibility = View.VISIBLE
//            setOnClickListener { finish() }
//        }
        webView = findViewById<WebView>(R.id.webView).apply {
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                setSupportZoom(true)
            }
        }

        intent.getStringExtra(urlParamKey)?.let {
            Log.i(TAG, it)
            webView.loadUrl(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val TAG = "WebviewActivity"
        const val urlParamKey = "KEY_KEY"
    }
}