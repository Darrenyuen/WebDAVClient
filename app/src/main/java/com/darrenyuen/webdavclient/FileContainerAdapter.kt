package com.darrenyuen.webdavclient

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.SyncStateContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toIcon
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.darrenyuen.downloader.normal.FileUtils
import com.darrenyuen.downloader.normal.NormalDownloader
import com.darrenyuen.webdavclient.util.FileUtil
import com.darrenyuen.webdavclient.widget.BottomDialog
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import okhttp3.internal.notify
import java.io.*

/**
 * Create by yuan on 2021/2/28
 */
class FileContainerAdapter(private val mContext: Context, private var mNode: FileTreeNode) : RecyclerView.Adapter<FileContainerAdapter.ViewHolder>() {

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
        Log.i(TAG, mNode.mChildren[position].mValue.path)
        var fileType = FileType.File
        when {
            mNode.mChildren[position].mValue.name.contains(".txt") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.txt)
            }
            mNode.mChildren[position].mValue.name.contains(".pdf") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.pdf)
            }
            mNode.mChildren[position].mValue.name.contains(".apk") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.apk)
            }
            mNode.mChildren[position].mValue.name.contains(".doc") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.word)
            }
            mNode.mChildren[position].mValue.name.contains(".jpg") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.image)
            }
            mNode.mChildren[position].mValue.name.contains(".mp4") -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.video)
            }
            else -> {
                holder.iconFileTypeIV.setImageResource(R.drawable.dir)
//            holder.fileSizeTV.visibility = View.GONE
                fileType = FileType.Dir
            }
        }
        holder.fileNameTV.text = mNode.mChildren[position].mValue.name
        if (fileType == FileType.File) {
            val size = mNode.mChildren[position].mValue.size
            holder.fileSizeTV.let {
                when {
                    size / 1024 == 0L -> it.text = size.toString() + "B"
                    size / (1024 * 1024) == 0L -> it.text = (size / 1024.0).toString() + "KB"
                    else -> it.text = (size / (1024.0 * 1024.0)).toString().substring(0, (size / (1024.0 * 1024.0)).toString().indexOf(".") + 2) + "MB"
                }
            }
        } else if (fileType == FileType.Dir) holder.fileSizeTV.visibility = View.GONE
        holder.fileTimeTV.text = mNode.mChildren[position].mValue.lastModified.toString().replace("GMT+08:00 ", "")
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
                            R.id.viewOnLine -> viewOnLine("http://119.29.176.115${mNode.mChildren[position].mValue.path}".replace("/webdav", ""))
                            R.id.download -> webDavOperation(WebDavOperation.Download, mNode.mChildren[position].mValue.path, mNode.mChildren[position].mValue.name)
                            R.id.rename -> webDavOperation(WebDavOperation.Rename, mNode.mChildren[position].mValue.path, mNode.mChildren[position].mValue.name)
                            R.id.copy -> webDavOperation(WebDavOperation.Copy, mNode.mChildren[position].mValue.path, mNode.mChildren[position].mValue.name)
                            R.id.detail -> webDavOperation(WebDavOperation.Detail, mNode.mChildren[position].mValue.path, mNode.mChildren[position].mValue.name)
                            R.id.delete -> {
                                webDavOperation(WebDavOperation.Delete, mNode.mChildren[position].mValue.path, mNode.mChildren[position].mValue.name)
                                mNode.mChildren.removeAt(position)
                            }
                        }
                        mBottomDialog?.dismiss()
                    }
                    .build()
            mBottomDialog?.show()
        }
//        val hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
//        val path = if (hasSDCard) {
        val path = mContext.getExternalFilesDir(null).toString() + File.separator + mNode.mChildren[position].mValue.name
//        } else {
//            mContext.getEx.toString() + File.separator + mNode.mChildren[position].mValue.name
//        }
        if (File(path).isFile && File(path).exists()) {
            holder.iconDoneIV.visibility = View.VISIBLE
        }
        holder.itemView.setOnClickListener {
            if (File(path).isFile && File(path).exists()) {
                Log.i(TAG, "file's size is ${File(path).length()}")
                mContext.startActivity(FileUtil.openFile(path, mContext))
            }
//            mOnItemClickListener?.onItemClick(mNode.mChildren[position], fileType)
        }
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

    private fun webDavOperation(operation: WebDavOperation, path: String, name: String) {
        val sardine = OkHttpSardine()
        sardine.setCredentials("dev", "yuan")
        when(operation) {
            WebDavOperation.Rename -> {
                InputDialogFragment().show((mContext as FragmentActivity).supportFragmentManager, TAG, name, path)
//                Thread {
//                    Log.i(TAG, "http://119.29.176.115$path")
//                    sardine.move("http://119.29.176.115$path", "http://119.29.176.115/66666.jpg")
//                }.start()

                
            }
            WebDavOperation.Download -> {
                Thread {
                    writeToDisk(sardine.get("http://119.29.176.115$path")) {
//                        "data/data/${mContext.packageName}/$name"
//                        val hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
//                        if (hasSDCard) {
//                            Environment.getExternalStorageDirectory().toString() + File.separator + name
//                            Environment.getExternalStorageDirectory().toString() + File.separator + System.currentTimeMillis() + ".jpg"
//                        } else {
//                            Environment.getDownloadCacheDirectory().toString() + File.separator + System.currentTimeMillis() + ".jpg"
                        mContext.getExternalFilesDir(null).toString() + File.separator + name
//                        mContext.filesDir.toString() + File.separator + name
//                        Environment.getExternalStorageState() + File.separator + name
//                        }
                    }

                    runOnUIThread {
                        Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show()
                        mBottomDialog?.dismiss()
                        notifyDataSetChanged()
                    }
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
        }
    }

    private fun writeToDisk(inputStream: InputStream, diskPath: () -> String) {
        Log.i(TAG, "diskPath is ${diskPath.invoke()}")

        val mBuilder = NotificationCompat.Builder(mContext, "").apply {
            setContentTitle("下载任务")
            setContentText("下载进度")
            setSmallIcon(R.drawable.head)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
        }

        val notificationManager: NotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = mContext.getString(R.string.app_name)
            val descriptionText = mContext.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.apply {
            mBuilder.setProgress(100, 0, false)
            notify(1, mBuilder.build())
            for (i in 1..10) {
                Thread.sleep(500)
                mBuilder.setProgress(100, 10 * i, false)
                notify(1, mBuilder.build())
            }
            mBuilder.setContentText("下载完成")
                .setProgress(0, 0, false)
            notify(1, mBuilder.build())
        }

        val buffer = ByteArray(1024 * 10)
        var len = -1
        try {
            RandomAccessFile(diskPath.invoke(), "rw").use { randomAccessFile ->
                BufferedInputStream(inputStream).use { bis ->
                    while (bis.read(buffer).also { len = it } != -1) {
                        randomAccessFile.write(buffer, 0, len)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message ?: "")
        }

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
        return mNode.mChildren.size
    }

    fun setData(node: FileTreeNode) {
        mNode = node
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
        fun onItemClick(node: FileTreeNode, type: FileType)
    }

}