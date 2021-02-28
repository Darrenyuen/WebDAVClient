package com.darrenyuen.webdavclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatViewInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sardine.SardineFactory
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlin.concurrent.thread

class DirCatalogActivity : AppCompatActivity() {

    val TAG = "DirCatalogActivity"

    private lateinit var mRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dir_catalog)
        mRecyclerView = findViewById(R.id.fileContainer)
        getDirList()
    }

    private fun getDirList() {
//        val sardine = SardineFactory.begin("dev", "yuan")
//        sardine.list("http://119.29.176.115/webdav/").forEach {
//            Log.i(TAG, it.path)
//        }
        Thread {
            val sardine = OkHttpSardine()
            val pathList = ArrayList<String>()
            sardine.setCredentials("dev", "yuan")
            sardine.list("http://119.29.176.115/webdav/").forEach {
                Log.i(TAG, it.path)
                pathList.add(it.path)
            }
            runOnUiThread{
                mRecyclerView.layoutManager = LinearLayoutManager(this)
                mRecyclerView.adapter = FileContainerAdapter(this, pathList)
            }
        }.start()
    }
}