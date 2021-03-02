package com.darrenyuen.webdavclient

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatViewInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sardine.SardineFactory
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlin.concurrent.thread

class DirCatalogActivity : AppCompatActivity() {

    val TAG = "DirCatalogActivity"

    private var currentPath: String = "/"
    private lateinit var pathTV: TextView
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: FileContainerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dir_catalog)
        pathTV = findViewById(R.id.path)
        mRecyclerView = findViewById(R.id.fileContainer)
        getDirRoot()
    }

    private fun getDirRoot() {
//        val sardine = SardineFactory.begin("dev", "yuan")
//        sardine.list("http://119.29.176.115/webdav/").forEach {
//            Log.i(TAG, it.path)
//        }
        Thread {
            val sardine = OkHttpSardine()
            val dataList = ArrayList<FileBean>()
            val pathList = ArrayList<String>()
            sardine.setCredentials("dev", "yuan")
//            sardine.list("http://119.29.176.115/webdav/").forEach {
//                Log.i(TAG, it.path)
//                dataList.add(FileBean(it.path, it.name, it.modified,  it.contentLength / (1024.0f * 1024)))
//                pathList.add(it.path)
//            }
            sardine.list("http://119.29.176.115/webdav/").first().apply {
                dataList.add(FileBean(path, name, modified, contentLength / (1024.0f * 1024)))
            }
            runOnUiThread{
                dataList.forEach {
                    Log.i(TAG, it.toString())
                }
                mRecyclerView.layoutManager = LinearLayoutManager(this)
                mAdapter = FileContainerAdapter(this, dataList)
                mAdapter.setOnItemClickListener(object : FileContainerAdapter.OnItemClickListener {
                    @SuppressLint("SetTextI18n")
                    override fun onItemClick(path: String, fileType: FileType) {
                        if (fileType == FileType.Dir) {
                            currentPath = path
                            pathTV.text = "当前路径：$path"
                            updateFileDirContent(path)
                        }
                    }
                })
                mRecyclerView.adapter = mAdapter
            }
        }.start()
    }

    private fun updateFileDirContent(path: String) {
        Thread {
            val sardine = OkHttpSardine()
            val dataList = ArrayList<FileBean>()
            sardine.setCredentials("dev", "yuan")
            sardine.list("http://119.29.176.115/webdav/").forEach {
                if (it.path.startsWith(path) && it.path != path) {
                    dataList.add(FileBean(it.path, it.name, it.modified, it.contentLength / (1024.0f * 1024)))
                }
            }
            runOnUiThread {
                mAdapter.setData(dataList)
            }
        }.start()
    }

    override fun onBackPressed() {
        if (currentPath == "/") {
            super.onBackPressed()
            return
        }
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"))
        Log.i(TAG, "currentPath: $currentPath")
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1)
        Log.i(TAG, "currentPath: $currentPath")
        pathTV.text = currentPath
        updateFileDirContent(currentPath)
    }
}