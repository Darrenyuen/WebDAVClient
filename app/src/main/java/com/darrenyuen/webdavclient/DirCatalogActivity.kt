package com.darrenyuen.webdavclient

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darrenyuen.webdavclient.widget.BottomDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine

class DirCatalogActivity : AppCompatActivity(), View.OnClickListener {

    val TAG = "DirCatalogActivity"

    private var currentPath: String = "/"
    private lateinit var pathTV: TextView
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: FileContainerAdapter
    private lateinit var mUploadBtn: FloatingActionButton

    private lateinit var fileTreeRoot: FileTreeNode

    private lateinit var mBottomDialog: BottomDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dir_catalog)
        pathTV = findViewById(R.id.path)
        mRecyclerView = findViewById(R.id.fileContainer)
        mUploadBtn = findViewById(R.id.uploadFileBtn)
        mUploadBtn.setOnClickListener(this)
        mBottomDialog = BottomDialog.Builder(this)
                .title("上传至我的WebDAV服务器")
                .orientation(BottomDialog.VERTICAL)
                .menu(R.menu.upload_bottom_dialog)
                .padding(5)
                .paddingInItem(10)
                .itemSize(18)
                .onItemClickListener {
                    Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                }
                .build()
        getDirRoot()
    }

    private fun getDirRoot() {
//        val sardine = SardineFactory.begin("dev", "yuan")
//        sardine.list("http://119.29.176.115/webdav/").forEach {
//            Log.i(TAG, it.path)
//        }
        Thread {
            val sardine = OkHttpSardine()
            sardine.setCredentials("dev", "yuan")
//            sardine.list("http://119.29.176.115/webdav/").forEach {
//                Log.i(TAG, it.path)
//                dataList.add(FileBean(it.path, it.name, it.modified,  it.contentLength / (1024.0f * 1024)))
//                pathList.add(it.path)
//            }
//            sardine.list("http://119.29.176.115/webdav/").first().apply {
//                dataList.add(FileBean(path, name, modified, contentLength / (1024.0f * 1024)))
//            }
            val dataList: ArrayList<FileBean> = ArrayList()
            sardine.list("http://119.29.176.115/webdav/").forEach {
                dataList.add(FileBean(it.path, it.name, it.modified, it.contentLength / (1024.0f * 1024)))
            }
            //generate a file tree
            dataList.forEach { it1 ->
                if (it1.path == "/webdav/") {
                    val tempList = ArrayList<FileTreeNode>()
                    dataList.forEach { it2 ->
                        if (it2.path != "/webdav/" && (it2.path.count('/') <= 3)) tempList.add(FileTreeNode(FileBean(it2.path, it2.name, it2.lastModified, it2.size)))
                    }
                    fileTreeRoot = FileTreeNode(FileBean(it1.path, it1.name, it1.lastModified, it1.size), tempList)
                }
            }
            runOnUiThread{
//                dataList.forEach {
//                    Log.i(TAG, it.toString())
//                }
                mRecyclerView.layoutManager = LinearLayoutManager(this)
                mAdapter = FileContainerAdapter(this, fileTreeRoot)
                mAdapter.setOnItemClickListener(object : FileContainerAdapter.OnItemClickListener {
                    @SuppressLint("SetTextI18n")
                    override fun onItemClick(node: FileTreeNode, type: FileType) {
                        if (type == FileType.Dir) {
                            currentPath = node.mValue.path
                            pathTV.text = "当前路径：$node.mValue.path"
                            updateFileDirContent(node)
                        }
                    }
                })
                mRecyclerView.adapter = mAdapter
            }
        }.start()
    }

    private fun updateFileDirContent(node: FileTreeNode) {
        mAdapter.setData(node)
//        Thread {
//            val sardine = OkHttpSardine()
//            val dataList = ArrayList<FileBean>()
//            sardine.setCredentials("dev", "yuan")
//            sardine.list("http://119.29.176.115/webdav/").forEach {
//                if (it.path.startsWith(path) && it.path != path) {
//                    dataList.add(FileBean(it.path, it.name, it.modified, it.contentLength / (1024.0f * 1024)))
//                }
//            }
//            runOnUiThread {
//                mAdapter.setData(dataList)
//            }
//        }.start()
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
//        updateFileDirContent()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.uploadFileBtn -> mBottomDialog.show()
        }
    }
}

fun String.count(char: Char): Int {
    var result = 0
    this.forEach {
        if (it == char) result++
    }
    return result
}