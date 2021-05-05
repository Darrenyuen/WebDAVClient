package com.darrenyuen.webdavclient

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.darrenyuen.sardine.DownloadListener
import com.darrenyuen.webdavclient.util.ClipboardUtil
import com.darrenyuen.webdavclient.util.FileUtil
import com.darrenyuen.webdavclient.util.StorageUtils
import com.darrenyuen.webdavclient.widget.BottomDialog
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Create by yuan on 2021/2/28
 */
class FileContainerAdapter(private val mContext: Context, private var mFileList: LinkedList<FileBean>) : RecyclerView.Adapter<FileContainerAdapter.ViewHolder>() {

    val TAG = "FileContainerAdapter"

    private var mOnItemClickListener: OnItemClickListener? = null

    private var mBottomDialog: BottomDialog? = null
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        mOnItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_file, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i(TAG, mFileList[position].name)
        var fileType = FileType.File
        var downloadFileType: StorageUtils.FileType
        when {
            mFileList[position].name.contains(".txt") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.txt)
                downloadFileType = StorageUtils.FileType.File
            }
            mFileList[position].name.contains(".pdf") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.pdf)
                downloadFileType = StorageUtils.FileType.File
            }
            mFileList[position].name.contains(".apk") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.apk)
                downloadFileType = StorageUtils.FileType.File
            }
            mFileList[position].name.contains(".doc") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.word)
                downloadFileType = StorageUtils.FileType.File
            }
            mFileList[position].name.contains(".jpg") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.image)
                downloadFileType = StorageUtils.FileType.Photo
            }
            mFileList[position].name.contains(".mp4") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.video)
                downloadFileType = StorageUtils.FileType.Video
            }
            else -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.dir)
//            holder.fileSizeTV.visibility = View.GONE
                fileType = FileType.Dir
                downloadFileType = StorageUtils.FileType.File
            }
        }
        holder.fileNameTV.text = mFileList[position].name
        WebDAVContext.getDBService().getFileHashData(mContext).forEach {
            if (it.fileName == mFileList[position].name && it.filePath == mFileList[position].path) {
                holder.iconDoneIV.visibility = View.VISIBLE
            }
        }
        if (fileType == FileType.File) {
            val size = mFileList[position].size
            holder.fileSizeTV.let {
                when {
                    size / 1024 == 0L -> it.text = size.toString() + "B"
                    size / (1024 * 1024) == 0L -> it.text = (size / 1024.0).toString().let {
                        it.substring(0, it.indexOf('.') + 3) + "KB"
                    }
                    else -> it.text = (size / (1024.0 * 1024.0)).toString().substring(0, (size / (1024.0 * 1024.0)).toString().indexOf(".") + 3) + "MB"
                }
            }
        } else if (fileType == FileType.Dir) holder.fileSizeTV.visibility = View.GONE
        holder.fileTimeTV.text = mFileList[position].lastModified.toString().replace("GMT+08:00 ", "")
        holder.moreIV.setOnClickListener {
            mBottomDialog = BottomDialog.Builder(mContext)
                    .title("请选择操作：")
                    .orientation(BottomDialog.VERTICAL)
                    .menu(R.menu.item_bottom_dialog)
                    .padding(5)
                    .paddingInItem(10)
                    .itemSize(18)
                    .onItemClickListener {
                        when (it.id) {
                            R.id.viewOnLine -> viewOnLine("http://119.29.176.115${mFileList[position].path}".replace("/webdav", ""))
//                            R.id.viewLocal -> viewOnLocal(mFileList[position].path, mFileList[position].name, downloadFileType)
                            R.id.download -> webDavOperation(WebDavOperation.Download, mFileList[position].path, mFileList[position].name, downloadFileType)
                            R.id.rename -> webDavOperation(WebDavOperation.Rename, mFileList[position].path, mFileList[position].name)
                            R.id.copy -> webDavOperation(WebDavOperation.Copy, mFileList[position].path, mFileList[position].name)
                            R.id.share -> {
                                ClipboardUtil.copyToClipboard("http://119.29.176.115${mFileList[position].path}".replace("/webdav", ""))
                                Toast.makeText(mContext, "已复制分享链接到粘贴板", Toast.LENGTH_SHORT).show()
                            }
//                            R.id.detail -> webDavOperation(WebDavOperation.Detail, mFileList[position].path, mFileList[position].name)
                            R.id.delete -> {
                                webDavOperation(WebDavOperation.Delete, mFileList[position].path, mFileList[position].name)
                                mFileList.removeAt(position)
                            }
                        }
                        mBottomDialog?.dismiss()
                    }
                    .build()
            mBottomDialog?.show()
        }
//        val hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
//        val path = if (hasSDCard) {
        val path = mContext.getExternalFilesDir(null).toString() + File.separator + mFileList[position].name
//        } else {
//            mContext.getEx.toString() + File.separator + mNode.mChildren[position].mValue.name
//        }
        if (File(path).isFile && File(path).exists()) {
            holder.iconDoneIV.visibility = View.VISIBLE
        }
        //Android R后不能通过File(path)形式访问文件了？
//        holder.itemView.setOnClickListener {
//            if (File(path).isFile && File(path).exists()) {
//                Log.i(TAG, "file's size is ${File(path).length()}")
//                mContext.startActivity(FileUtil.openFile(path, mContext))
//            }
//            val targetFileList = LinkedList<FileBean>()
//            mFileList.forEach {
//                if (it.path.startsWith(mFileList[position].path)) {
//                    targetFileList.add(it)
//                }
//            }
//            mOnItemClickListener?.onItemClick(mFileList[position], targetFileList, fileType)
//        }
    }

    private fun viewOnLine(url: String) {
        if (url.contains(".doc") || url.contains(".e")) {
            val uri = Uri.parse(url)
            mContext.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } else {
            val intent = Intent(mContext, WebviewActivity::class.java)
            intent.putExtra(WebviewActivity.urlParamKey, url)
            mContext.startActivity(intent)
        }
    }

    private fun viewOnLocal(path: String, name: String, fileType: StorageUtils.FileType) {
        var flag = false
        WebDAVContext.getDBService().getFileHashData(mContext).forEach {
            if (it.fileName == name && it.filePath == path) {
                flag = true
            }
        }
        if (!flag) {
            Toast.makeText(mContext, "该文件还未下载", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = when (fileType) {
            StorageUtils.FileType.File -> {
                FileProvider.getUriForFile(mContext, "webdavclient.fileprovider", File(name))
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                }
                mContext.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            }
            StorageUtils.FileType.Video -> {
                FileProvider.getUriForFile(mContext, "webdavclient.fileprovider", File(name))
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                }
                mContext.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            }
            StorageUtils.FileType.Photo -> {
                FileProvider.getUriForFile(mContext, "webdavclient.fileprovider", File(name))
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                }
                mContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            }
        }
        mContext.startActivity(FileUtil.openFile(uri!!, name, mContext))
    }

    private fun webDavOperation(operation: WebDavOperation, path: String, name: String, downloadFileType: StorageUtils.FileType ?= null) {
        val sardine = com.darrenyuen.sardine.impl.OkHttpSardine()
        sardine.setCredentials("dev", "yuan")
        Log.i(TAG, "webDavOperation() >>> path: $path")
        when(operation) {
            WebDavOperation.Rename -> {
                InputDialogFragment().apply {
                    arguments = Bundle().apply { putString(InputDialogFragment.OP, InputDialogFragment.RENAME_OP) }
                }.show((mContext as FragmentActivity).supportFragmentManager, TAG, path, name)
            }
            WebDavOperation.Download -> {
                Thread {
//                    val downloadListener = object : DownloadListener {
//                        override fun onProgress(progress: Float) {
//                            runOnUIThread {
//                                notificationManager.apply {
//                                    mBuilder.setContentText("下载进度：$progress").setProgress(100, progress.toInt(), false)
//                                    notify(1, mBuilder.build())
//                                }
//                            }
//                        }
//
//                        override fun onSuccess() {
//                            runOnUIThread {
//                                notificationManager.apply {
//                                    mBuilder.setContentText("下载完成")
//                                            .setProgress(0, 0, false)
//                                    notify(1, mBuilder.build())
//                                }
//                            }
//                        }
//
//                        override fun onFailure(errMsg: String?) {
//
//                        }
//                    }
//                    sardine.get("http://119.29.176.115$path", Environment.getExternalStorageDirectory().absolutePath + File.separator + name, downloadListener)

                    val notificationManager: NotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val mBuilder = NotificationCompat.Builder(mContext, "channelId").apply {
                        setContentTitle("下载任务")
                        setSmallIcon(R.drawable.head)
                        priority = NotificationCompat.PRIORITY_DEFAULT
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val name = mContext.getString(R.string.app_name)
                        val descriptionText = mContext.getString(R.string.app_name)
                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                        val channel = NotificationChannel("channelId", name, importance).apply {
                            description = descriptionText
                        }
                        // Register the channel with the system
                        notificationManager.createNotificationChannel(channel)
                    }

                    var totalSize = 0
                    val url = "http://119.29.176.115$path".replace("/webdav", "")
                    val httpURLConnection = URL(url).openConnection() as HttpURLConnection
                    httpURLConnection.apply {
                        totalSize = contentLength
                        disconnect()
                    }
                    WebDAVContext.getDBService().getFileHashData(mContext).forEach {
                        if (it.fileName == name && it.filePath == path) {
                            runOnUIThread {
                                Toast.makeText(mContext, "该文件已下载到本地", Toast.LENGTH_SHORT).show()
                            }
                            return@Thread
                        }
                    }

                    StorageUtils.storageFile(mContext, sardine.get("http://119.29.176.115$path"), totalSize, name, downloadFileType!!, object : StorageUtils.StorageFileListener {
                        override fun onSuccess() {
                            mBuilder.setProgress(0, 0,false)
                            mBuilder.setContentText("下载完成")
                            notificationManager.notify(1, mBuilder.build())
                            WebDAVContext.getDBService().writeFileHashData(mContext, path, name)
                            runOnUIThread {
                                Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show()
                                mBottomDialog?.dismiss()
                                notifyDataSetChanged()
                            }
                        }

                        override fun onFailure(msg: String?) {

                        }

                        override fun onBegin() {
                            runOnUIThread {
                                Toast.makeText(mContext, "开始下载", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onProgress(progress: Double) {
                            mBuilder.setContentText("下载进度")
                            mBuilder.setProgress(100, progress.toInt(),false)
                            notificationManager.notify(1, mBuilder.build())
                        }
                    })

//                    writeToDisk(sardine.get("http://119.29.176.115$path"), name)
//                    {
//                        val hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
//                        if (hasSDCard) {
//                            Environment.getExternalStorageDirectory().toString() + File.separator + name
//                        } else {
//                            Environment.getDownloadCacheDirectory().toString() + File.separator + name
//                        }
//                    }
                }.start()
            }
            WebDavOperation.Delete -> {
                Thread {
                    sardine.delete("http://119.29.176.115$path")
                    runOnUIThread {
                        Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show()
                        mBottomDialog?.dismiss()
                        notifyDataSetChanged()
                    }
                }.start()
            }
            WebDavOperation.Detail -> {

            }
        }
    }

    private fun writeToDisk(inputStream: InputStream, name: String) {
//        Log.i(TAG, "diskPath is ${diskPath.invoke()}")

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/WebDAVClient")//保存路径
                put(MediaStore.MediaColumns.IS_PENDING, true)
            }
        }
        val insert = mContext.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ) ?: return //为空的话 直接失败返回了

        //这个打开了输出流  直接保存图片就好了
        mContext.contentResolver.openOutputStream(insert).use { ons ->
            inputStream.use { ins ->
                val buf = ByteArray(2048)
                var len: Int
                while (ins.read(buf).also { len = it } != -1) {
                    ons?.write(buf, 0, len)
                }
                ons?.flush()
            }
//            inputStream.copyTo(it!!)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, false)
        }



//        val mBuilder = NotificationCompat.Builder(mContext, "").apply {
//            setContentTitle("下载任务")
//            setContentText("下载进度")
//            setSmallIcon(R.drawable.head)
//            priority = NotificationCompat.PRIORITY_DEFAULT
//            setAutoCancel(true)
//        }
//
//        val notificationManager: NotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = mContext.getString(R.string.app_name)
//            val descriptionText = mContext.getString(R.string.app_name)
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel("", name, importance).apply {
//                description = descriptionText
//            }
//            // Register the channel with the system
//            notificationManager.createNotificationChannel(channel)
//        } else {
//
//        }

//        notificationManager.apply {
//            mBuilder.setProgress(100, 0, false)
//            notify(1, mBuilder.build())
//            val buffer = ByteArray(1024 * 10)
//            var len = -1
//            var i = 1
//            try {
//                RandomAccessFile(diskPath.invoke(), "rw").use { randomAccessFile ->
//                    BufferedInputStream(inputStream).use { bis ->
//                        while (bis.read(buffer).also { len = it } != -1) {
//                            randomAccessFile.write(buffer, 0, len)
//                            if (i >= 10) mBuilder.setProgress(0, 0,false)
//                            else mBuilder.setProgress(100, (i++) * 10 ,false)
//                            notify(1, mBuilder.build())
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Log.e(TAG, e.message ?: "")
//            }
//            mBuilder.setContentText("下载完成")
//                .setProgress(0, 0, false)
//            notify(1, mBuilder.build())
//        }

//        val buffer = ByteArray(1024 * 10)
//        var len = -1
//        try {
//            RandomAccessFile(diskPath.invoke(), "rw").use { randomAccessFile ->
//                BufferedInputStream(inputStream).use { bis ->
//                    while (bis.read(buffer).also { len = it } != -1) {
//                        randomAccessFile.write(buffer, 0, len)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e(TAG, e.message ?: "")
//        }

//        FileUtils.getFileContentLength(diskPath.invoke())
//        val destFile = File(diskPath.invoke())
//        if (!destFile.exists()) destFile.createNewFile()
//        Log.i(TAG, "name is ${destFile.name} ${destFile.exists()} ${destFile.isFile}")
//        val fos = FileOutputStream(destFile)
//        val byte = ByteArray(1024)
//        var byteCount = 0
//        var bytesWritten = 0
//        while ((inputStream.read(byte).also { byteCount = it }) != -1) {
//            fos.write(byte, bytesWritten, byteCount)
//            bytesWritten += byteCount
//        }
//        inputStream.close()
//        fos.close()
    }

    private fun runOnUIThread(action: () -> Unit) {
        mHandler.post(action)
    }

    override fun getItemCount(): Int {
        return mFileList.size
    }

    fun setData(fileList: LinkedList<FileBean>) {
        mFileList = fileList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconFileTypeIV: ImageView = itemView.findViewById(R.id.icon_file_type)
        val iconDoneIV = itemView.findViewById<ImageView>(R.id.icon_done)
        val fileNameTV = itemView.findViewById<TextView>(R.id.fileName)
        val fileSizeTV = itemView.findViewById<TextView>(R.id.fileSize)
        val fileTimeTV = itemView.findViewById<TextView>(R.id.time)
        val moreIV = itemView.findViewById<ImageView>(R.id.more)
    }

    interface OnItemClickListener {
        fun onItemClick(rootFile: FileBean, fileList: LinkedList<FileBean>, type: FileType)
    }

}