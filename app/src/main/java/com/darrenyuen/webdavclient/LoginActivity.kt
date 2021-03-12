package com.darrenyuen.webdavclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor

class LoginActivity : AppCompatActivity() {

    private lateinit var flutterEngine: FlutterEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = FlutterFragment.withNewEngine().initialRoute("login").build<FlutterFragment>()
        fragmentTransaction.replace(R.id.fl_container, fragment)
        fragmentTransaction.commit()
        val flutterView = FlutterView(this)
        val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val container = findViewById<RelativeLayout>(R.id.fl_container)
//        container.addView(flutterView, lp)
        flutterEngine = FlutterEngine(this).apply {
            dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            navigationChannel.setInitialRoute("launch by native page, display by flutter page")

        }.also {
            flutterView.attachToFlutterEngine(it)
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