package com.darrenyuen.webdavclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.sardine.SardineFactory
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlin.concurrent.thread

class DirCatalogActivity : AppCompatActivity() {

    val TAG = "DirCatalogActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dir_catalog)
        getDirList()
    }

    private fun getDirList() {
//        val sardine = SardineFactory.begin("dev", "yuan")
//        sardine.list("http://119.29.176.115/webdav/").forEach {
//            Log.i(TAG, it.path)
//        }
        Thread {
            val sardine = OkHttpSardine()
            sardine.setCredentials("dev", "yuan")
            sardine.list("http://119.29.176.115/webdav/").forEach {
                Log.i(TAG, it.path)
            }
        }.start()
    }
}