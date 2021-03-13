package com.darrenyuen.webdavclient.util

import io.flutter.embedding.android.FlutterFragment

/**
 * Create by yuan on 2021/3/13
 */
object FlutterRoute {
    fun generateFlutterFragment(route: String): FlutterFragment {
        return FlutterFragment.withNewEngine().initialRoute(route).build<FlutterFragment>()
    }
}