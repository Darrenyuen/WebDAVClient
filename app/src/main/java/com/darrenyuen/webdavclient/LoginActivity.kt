package com.darrenyuen.webdavclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.darrenyuen.webdavclient.util.FlutterRoute
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class LoginActivity : AppCompatActivity() {

    private lateinit var flutterEngine: FlutterEngine

    companion object {
        const val TAG = "LoginActivity"
        const val CHANNEL_NATIVE_CATALOG = "com.darrenyuen.webDAVClient/catalog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

//        val fragmentTransaction = supportFragmentManager.beginTransaction()
//        val fragment = FlutterRoute.generateFlutterFragment("login")
//        fragmentTransaction.replace(R.id.fl_container, fragment)
//        fragmentTransaction.commit()
//
//        MethodChannel(fragment., CHANNEL_NATIVE_CATALOG)

        val flutterView = FlutterView(this)
        val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val container = findViewById<RelativeLayout>(R.id.fl_container)
        container.addView(flutterView, lp)
        flutterEngine = FlutterEngine(this).apply {
            dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            navigationChannel.setInitialRoute("login")
        }.also {
            flutterView.attachToFlutterEngine(it)
        }

        MethodChannel(flutterEngine.dartExecutor, CHANNEL_NATIVE_CATALOG).setMethodCallHandler { call, result ->
            Log.i(TAG, "msg: ${call.argument<String>("message")}")
            when (call.method) {
                CHANNEL_NATIVE_CATALOG -> {
                    startActivity(Intent(this, DirCatalogActivity::class.java))
                    finish()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        flutterEngine.lifecycleChannel.appIsResumed()
    }

    override fun onPause() {
        super.onPause()
        flutterEngine.lifecycleChannel.appIsInactive()
    }

    override fun onStop() {
        super.onStop()
        flutterEngine.lifecycleChannel.appIsPaused()
    }
}