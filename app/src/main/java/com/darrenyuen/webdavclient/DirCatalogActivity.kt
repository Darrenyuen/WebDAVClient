package com.darrenyuen.webdavclient

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toFile
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darrenyuen.guide.GuideView
import com.darrenyuen.guide.HighLightShape
import com.darrenyuen.webdavclient.util.ConvertUtil
import com.darrenyuen.webdavclient.util.FileUtil
import com.darrenyuen.webdavclient.widget.BottomDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import java.io.File
import java.io.InputStream

class DirCatalogActivity : AppCompatActivity(), View.OnClickListener, InputDialogFragment.Callback {

    val TAG = "DirCatalogActivity"

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var userNameTV: TextView
    private lateinit var menuIV: ImageView
    private var currentPath: String = "/"
    private lateinit var pathTV: TextView
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: FileContainerAdapter
    private lateinit var mUploadBtn: FloatingActionButton

    private lateinit var fileTreeRoot: FileTreeNode

    private lateinit var mBottomDialog: BottomDialog

    companion object {
        const val TAKE_PHOTO = 0
        const val UPLOAD_PHOTO = 1
        const val UPLOAD_VIDEO = 2
        const val UPLOAD_SD_FILE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dir_catalog)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigation_container)
        userNameTV = navigationView.getHeaderView(0).findViewById(R.id.userNameTV)
        menuIV = findViewById<ImageView>(R.id.menuIcon).apply {
            setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
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
                .onItemClickListener { it ->
                    when (it.id) {
                        R.id.takePhoto -> {
                            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PHOTO)
                        }
                        R.id.uploadPhoto -> {
                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "image/*"
                            }
                            startActivityForResult(intent, UPLOAD_PHOTO)
                        }
                        R.id.uploadVideo -> {
                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "video/*"
                            }
                            startActivityForResult(intent, UPLOAD_VIDEO)
                        }
                        R.id.uploadSDCardFile -> {
                            Intent(Intent.ACTION_GET_CONTENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }.let {  intent ->
                                startActivityForResult(intent, UPLOAD_SD_FILE)
                            }

                        }
                    }
                    mBottomDialog.dismiss()
                }
                .build()
        initEvent()
        getDirRoot()
    }

    private fun initEvent() {
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> {
                    WebDAVContext.getDBService().clearAllData(this)
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        userNameTV.text = WebDAVContext.getDBService().getUserInfo(this).account
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        var guideViewForUploadBtn = GuideView.Builder.newInstance(this)
            .setTargetView(mUploadBtn)
            .setShape(HighLightShape.CIRCLE)
            .setBgColor(resources.getColor(R.color.shadow))
            .setOnClickListener(object : GuideView.OnClickListener {
                override fun onClick(guideView: GuideView) {
                    guideView.hide()
                }
            })
            .build()
        guideViewForUploadBtn.show()
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
                dataList.add(FileBean(it.path, it.name, it.modified, it.contentLength))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                TAKE_PHOTO -> {
                    Log.i(TAG, "onActivityResult() >>> ${data?.extras?.get("data")}")
                    data?.extras?.get("data")?.let {
                        upload(ConvertUtil.bitmapToByteArray(it as Bitmap), "${System.currentTimeMillis()}.jpg")
                    }
                }
                UPLOAD_PHOTO -> {
                    data?.data?.let {
    //                    val images = arrayOf(MediaStore.Images.Media.DATA)
    //                    val cursor = this
    //                    val cursor = this.managedQuery(it, images, null, null, null)
    //                    val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    //                    cursor.moveToFirst()
    //                    Log.i(TAG, "index is $index, cursor getString is : ${cursor.getString(index)}")
    //                    uploadBitmap(BitmapFactory.decodeFile(cursor.getString(index)))
                        this.contentResolver.openInputStream(it)?.let { it1 -> upload(it1.readBytes(), "${System.currentTimeMillis()}.jpg") }
                    }
                }
                UPLOAD_VIDEO -> {
                    data?.data?.let {
                        this.contentResolver.openInputStream(it)?.let { it1 ->
                            upload(it1.readBytes(), "${System.currentTimeMillis()}.mp4")
                        }
                    }
                }
                UPLOAD_SD_FILE -> {
                    data?.data?.let {
                        this.contentResolver.openInputStream(it)?.let { it1 ->
                            val cr = contentResolver.query(it, null, null, null, null, null)
                            val fileName = cr?.let {cursor ->
                                cursor.moveToFirst()
                                val displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                cursor.close()
                                displayName
                            }?:"${System.currentTimeMillis()}.${MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(it))}}"
                            Log.i(TAG, "fileName is : $fileName")
                            upload(it1.readBytes(), fileName)
                        }
                    }
                }
            }
        }
    }

    private fun upload(byteArray: ByteArray, fileName: String) {
        Thread {
            val sardine = OkHttpSardine()
            sardine.setCredentials("dev", "yuan")
            val url = "http://119.29.176.115/webdav/$fileName"
            sardine.put(url, byteArray)
            runOnUiThread{
                Toast.makeText(this, "上传成功", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    override fun onClickForRename(newName: String, oldName: String, path: String) {
        Thread {
            Log.i(TAG, "newName is $newName, oldName is $oldName, path is $path")
            val format = oldName.substring(oldName.indexOf("."))
            var sardine = OkHttpSardine()
            sardine.setCredentials("dev", "yuan")
            val oriUrl = "http://119.29.176.115/webdav/$oldName"
            val desUrl = "http://119.29.176.115/webdav/$newName$format"
            if (sardine.exists(desUrl)) {
                sardine.move(oriUrl, desUrl)
                runOnUiThread {
                    Toast.makeText(this, "重命名成功", Toast.LENGTH_SHORT).show()
                }
            } else {
                sardine.put(desUrl, byteArrayOf())
                sardine.move(oriUrl, desUrl)
                runOnUiThread {
                    Toast.makeText(this, "重命名成功", Toast.LENGTH_SHORT).show()
                }
            }
            Log.i(TAG, "oriUrl is $oriUrl, desUrl is $desUrl")
        }.start()
    }
}

fun String.count(char: Char): Int {
    var result = 0
    this.forEach {
        if (it == char) result++
    }
    return result
}