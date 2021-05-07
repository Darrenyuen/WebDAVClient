package com.darrenyuen.webdavclient.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.darrenyuen.webdavclient.*
import com.darrenyuen.webdavclient.bean.FileBean
import com.darrenyuen.webdavclient.bean.FileTreeNode
import com.darrenyuen.webdavclient.util.ConvertUtil
import com.darrenyuen.webdavclient.util.NetworkUtil
import com.darrenyuen.webdavclient.widget.BottomDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import java.util.*
import kotlin.collections.ArrayList

class DirCatalogActivity : AppCompatActivity(), View.OnClickListener, InputDialogFragment.Callback {

    val TAG = "DirCatalogActivity"

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var userNameTV: TextView
    private lateinit var menuIV: ImageView
    private lateinit var usedTV: TextView

    /**
     * 真实的当前文件路径，初始时为/webdav
     */
    private var currentPath: String = "/webdav/"

    /**
     * 展示出来的文件路径，需去掉开头的 /webdav 字符串
     */
    private lateinit var pathTV: TextView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: FileContainerAdapter
    private lateinit var mSearchFileBtn: FloatingActionButton
    private lateinit var mUploadBtn: FloatingActionButton

    private lateinit var fileTreeRoot: FileTreeNode

    private lateinit var mBottomDialog: BottomDialog
    private var isShowingSearch = false


    val dataList: ArrayList<FileBean> = ArrayList()

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
        usedTV = navigationView.getHeaderView(0).findViewById(R.id.usedTV)
        menuIV = findViewById<ImageView>(R.id.menuIcon).apply {
            setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        pathTV = findViewById(R.id.path)
        refreshLayout = findViewById(R.id.refreshLayout)
        mRecyclerView = findViewById(R.id.fileContainer)
        mSearchFileBtn = findViewById(R.id.searchFileBtn)
        mUploadBtn = findViewById(R.id.uploadFileBtn)
        mSearchFileBtn.setOnClickListener(this)
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
                            }.let { intent ->
                                startActivityForResult(intent, UPLOAD_SD_FILE)
                            }

                        }
                        R.id.createDir -> {
                            InputDialogFragment().apply {
                                arguments = Bundle().apply { putString(InputDialogFragment.OP, InputDialogFragment.CREATE_DIR_OP) }
                            }.show(supportFragmentManager, TAG, currentPath)
                        }
                        R.id.createFile -> {
                            InputDialogFragment().apply {
                                arguments = Bundle().apply { putString(InputDialogFragment.OP, InputDialogFragment.CREATE_FILE_OP) }
                            }.show(supportFragmentManager, TAG, currentPath)
                        }
                    }
                    mBottomDialog.dismiss()
                }
                .build()
        initEvent()
        getFileList(currentPath)
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
        refreshLayout.setOnRefreshListener {
            getFileList(currentPath)

        }
        userNameTV.text = WebDAVContext.getDBService().getUserInfo(this).account
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        // 使用文字
        val tv = TextView(this)
        tv.text = "更多操作"
        tv.setTextColor(resources.getColor(R.color.black))
        tv.textSize = 20f
        tv.gravity = Gravity.CENTER

//        val guideViewForUploadBtn = GuideView.Builder.newInstance(this)
//                .setTargetView(mUploadBtn)
//                .setShape(HighLightShape.CIRCLE)
//                .setBgColor(resources.getColor(R.color.shadow))
//                .setCustomGuideView(tv)
//                .setDirection(Direction.TOP)
//                .setOnClickListener(object : GuideView.OnClickListener {
//                    override fun onClick(guideView: GuideView) {
//                        guideView.hide()
//                    }
//                })
//                .build()
//        guideViewForUploadBtn.show()
    }

    private fun getFileList(parentPath: String) {

        if (!NetworkUtil.isNetworkAvailable(this)) return
        pathTV.visibility = View.VISIBLE
        dataList.clear()
        Thread {
            val sardine = OkHttpSardine()
            sardine.setCredentials("dev", "yuan")
            var usedSize = 0L
            sardine.list("http://119.29.176.115$currentPath").forEach {
                Log.i(TAG, "path: ${it.path}")
                usedSize += it.contentLength
                dataList.add(FileBean(it.path, it.name, it.modified, it.contentLength))
            }
            runOnUiThread{
                refreshLayout.isRefreshing = false
                when {
                    usedSize / 1024 == 0L -> usedTV.text = usedSize.toString() + "B"
                    usedSize / (1024 * 1024) == 0L -> usedTV.text = (usedSize / 1024.0).toString().let {
                        it.substring(0, it.indexOf('.') + 3) + "KB"
                    }
                    else -> usedTV.text = (usedSize / (1024.0 * 1024.0)).toString().substring(0, (usedSize / (1024.0 * 1024.0)).toString().indexOf(".") + 3) + "MB"
                }
                mRecyclerView.layoutManager = LinearLayoutManager(this)
                val fileList = LinkedList<FileBean>()
                dataList.forEach {
                    if (it.path.startsWith(parentPath) && it.path.count('/') <= parentPath.count('/') + 1 && it.path != parentPath) fileList.add(it)
                }
                mAdapter = FileContainerAdapter(this, fileList)
                mAdapter.setOnItemClickListener(object : FileContainerAdapter.OnItemClickListener {
                    @SuppressLint("SetTextI18n")
                    override fun onItemClick(rootFile: FileBean, fileList: LinkedList<FileBean>, type: FileType) {
                        if (type == FileType.Dir) {
                            currentPath = rootFile.path
                            pathTV.text = "当前路径：${currentPath.replace("/webdav", "")}"
//                            val numOfDeChar = rootFile.path.count('/')
//                            val tempFileList = LinkedList<FileBean>()
                            getFileList(currentPath)
//                            fileList.forEach {
//                                if (it.path.startsWith(rootFile.path) && it.path.count('/') <= numOfDeChar + 1 && it.path != rootFile.path) tempFileList.add(it)
//                            }
//                            updateFileDirContent(tempFileList)
                        }
                    }
                })
                mRecyclerView.adapter = mAdapter
            }
        }.start()
    }

    private fun updateFileDirContent(fileList: LinkedList<FileBean>) = mAdapter.setData(fileList)

    override fun onBackPressed() {
        if (isShowingSearch) {
            getFileList("/webdav/")
            isShowingSearch = false
            return
        }
        if (currentPath == "/webdav/") {
            super.onBackPressed()
            return
        }
        pathTV.visibility = View.VISIBLE
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"))
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1)
        pathTV.text = "当前路径：" + currentPath.replace("/webdav", "")
        getFileList(currentPath)
//        Thread {
//            val sardine = com.darrenyuen.sardine.impl.OkHttpSardine()
//            sardine.setCredentials("dev", "yuan")
//            val dataList = LinkedList<FileBean>()
//            sardine.list("http://119.29.176.115/webdav/").forEach {
//                Log.i(TAG, "path: ${it.path}")
//                dataList.add(FileBean(it.path, it.name, it.modified, it.contentLength))
//            }
//            runOnUiThread {
//                val fileList = LinkedList<FileBean>()
//                dataList.forEach {
//                    if (it.path.startsWith(currentPath) && it.path.count('/') <= currentPath.count('/') + 1 && it.path != "/webdav/") fileList.add(it)
//                }
//                updateFileDirContent(fileList)
//            }
//        }.start()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.uploadFileBtn -> mBottomDialog.show()
            R.id.searchFileBtn -> {
                InputDialogFragment().apply {
                    arguments = Bundle().apply { putString(InputDialogFragment.OP, InputDialogFragment.SEARCH_FILE) }
                }.show(supportFragmentManager, TAG, currentPath)
            }
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
                            val fileName = cr?.let { cursor ->
                                cursor.moveToFirst()
                                val displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                cursor.close()
                                displayName
                            } ?: "${System.currentTimeMillis()}.${MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(it))}}"
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
            val oriUrl = "http://119.29.176.115$path"
            val adjustPath = path.substring(0, path.lastIndexOf('/'))
            val desUrl = "http://119.29.176.115$adjustPath/$newName$format"
            Log.i(TAG, "oriUrl: $oriUrl, desUrl: $desUrl")
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

    override fun onClickForCreateDir(path: String, dirName: String) {
        Thread {
            Log.i(TAG, "onClickForCreateDir() >>> path: $path, dirName: $dirName")
            val sardine = OkHttpSardine()
            sardine.setCredentials("dev", "yuan")
            sardine.createDirectory("http://119.29.176.115$path$dirName")
            runOnUiThread {
                Toast.makeText(this, "创建成功", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    override fun onClickForCreateFile(path: String, fileName: String) {
        Thread {
            Log.i(TAG, path)
            val sardine = OkHttpSardine()
            sardine.setCredentials("dev", "yuan")
            val newFileUrl = "http://119.29.176.115$path$fileName"
            sardine.put(newFileUrl, byteArrayOf())
            runOnUiThread {
                Toast.makeText(this, "创建成功", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    override fun onSearchFile(fileNameText: String) {
        isShowingSearch = true
        Log.i(TAG, "onSearchFile() >>>> $fileNameText")
        if (fileNameText.isEmpty()) {
            Toast.makeText(this, "文件名不能为空", Toast.LENGTH_SHORT).show()
        }
        val fileList = LinkedList<FileBean>()
        dataList.forEach {
            if (it.name.contains(fileNameText)) fileList.add(it)
        }
        pathTV.visibility = View.GONE
        mAdapter = FileContainerAdapter(this, fileList)
        mRecyclerView.adapter = mAdapter
    }
}

fun String.count(char: Char): Int {
    var result = 0
    this.forEach {
        if (it == char) result++
    }
    return result
}