package com.darrenyuen.webdavclient

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.darrenyuen.downloader.DownloadListener
import com.darrenyuen.downloader.DownloaderFactory
import com.darrenyuen.downloader.DownloaderType
import com.github.sardine.SardineFactory
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterFragment
import kotlinx.coroutines.*
import java.io.File


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "MainActivity"

    private lateinit var urlET: EditText
    private lateinit var downloadBtn: Button
    private lateinit var showDirBtn: Button
    private lateinit var toDirBtn: Button
    private lateinit var toFlutterBtn: Button;

    private var fileName: String = ""

    private val job = Job()
    val scope = CoroutineScope(job)

    private val downloaderFactory = DownloaderFactory()

    private val downloadListener = object : DownloadListener {

        override fun onProgress(progress: Float) {
            runOnUiThread {
                Log.i(TAG, "onProgress() >>> $progress")
            }
        }

        override fun onSuccess() {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "下载成功，保存路径为：${generateFileName()}", Toast.LENGTH_LONG).show()
                Log.i(TAG, fileName)
            }
        }

        override fun onFailure(errMsg: String?) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, errMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        urlET = findViewById(R.id.urlET)
        downloadBtn = findViewById<Button>(R.id.download).apply { setOnClickListener(this@MainActivity) }
//        showDirBtn = findViewById<Button>(R.id.showDir).apply { setOnClickListener(this@MainActivity) }
        toDirBtn = findViewById<Button>(R.id.toDir).apply { setOnClickListener(this@MainActivity) }
        toFlutterBtn = findViewById<Button>(R.id.toFlutter).apply { setOnClickListener(this@MainActivity) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.download -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    val permissions = arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    if (!hasPermissions(this, permissions)) {
                        ActivityCompat.requestPermissions(
                            this,
                            permissions,
                            112
                        )
                    } else {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val downloader = downloaderFactory.createDownloader(DownloaderType.Normal_downloader)
                                downloader.download(urlET.text.toString(), generateFileName(), downloadListener)
                            }
                        }
                    }
                } else {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val downloader = downloaderFactory.createDownloader(DownloaderType.Normal_downloader)
                            downloader.download(urlET.text.toString(), generateFileName(), downloadListener)
                        }
                    }
                }
            }
//            R.id.showDir -> {
//                SardineFactory.begin("dev", "yuan").list("http://119.29.176.115/webdav/").forEach {
//                    Log.i(TAG, it.path)
//                }
////                startActivity(Intent("com.android.camera.action.CROP").apply { setDataAndType(Uri.parse("content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20210122_170637.jpg"), "image/*") })
//            }
            R.id.toDir -> {
                startActivity(Intent(this, DirCatalogActivity::class.java))
            }
            R.id.toFlutter -> {
//                startActivity(FlutterActivity.createDefaultIntent(this))
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            112 -> {
                if (grantResults.isNotEmpty() && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val downloader = downloaderFactory.createDownloader(DownloaderType.Normal_downloader)
                            downloader.download(urlET.text.toString(), generateFileName(), downloadListener)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "The app was not allowed to read your store.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun generateFileName(): String {
        val hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
        if (hasSDCard) {
            return Environment.getExternalStorageDirectory().toString() + File.separator + System.currentTimeMillis() + ".jpg"
        } else {
            return Environment.getDownloadCacheDirectory().toString() + File.separator + System.currentTimeMillis() + ".jpg"
        }
    }
}