package com.darrenyuen.webdavclient.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.InputStream
import java.io.OutputStream

/**
 * Create by yuan on 2021/5/5
 */
object StorageUtils {

    enum class FileType{
        File,
        Video,
        Photo
    }

    fun storageFile(context: Context, inputStream: InputStream, totalSize: Int, fileName: String, type: FileType, listener: StorageFileListener? = null): Boolean {
        return when (type) {
            FileType.File -> {
                storageFile(context, inputStream, totalSize, fileName, listener)
            }
            FileType.Video -> {
                storageVideo(context, inputStream, totalSize, fileName, listener)
            }
            FileType.Photo -> {
                storagePhoto(context, inputStream, totalSize, fileName, listener)
            }
        }
    }

    private fun storagePhoto(context: Context, inputStream: InputStream, totalSize: Int, fileName: String, listener: StorageFileListener? = null): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/WebDAVClient")//保存路径
//                put(MediaStore.MediaColumns.IS_PENDING, true)
//            }
        }
        val insert = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return false
        context.contentResolver.openOutputStream(insert).use { ops ->
            ops ?: return false
            return writeToDisk(inputStream, ops, totalSize, listener)
        }
    }

    private fun storageVideo(context: Context, inputStream: InputStream, totalSize: Int, fileName: String, listener: StorageFileListener? = null): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        }
        val insert = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return false
        context.contentResolver.openOutputStream(insert).use { ops ->
            ops ?: return false
            return writeToDisk(inputStream, ops, totalSize, listener)
        }
    }

    private fun storageFile(context: Context, inputStream: InputStream, totalSize: Int, fileName: String, listener: StorageFileListener? = null): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        }
        val insert = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return false
        context.contentResolver.openOutputStream(insert).use { ops ->
            ops ?: return false
            return writeToDisk(inputStream, ops, totalSize, listener)
        }
    }

    private fun writeToDisk(inputStream: InputStream, outputStream: OutputStream, totalSize: Int, listener: StorageFileListener?): Boolean {
        inputStream.use { inputStream ->
            val buf = ByteArray(2048)
            var len: Int
            var writedSize = 0.0
            listener?.onBegin()
            while (inputStream.read(buf).also { len = it } != -1) {
                outputStream.write(buf, 0, len)
                writedSize += len
                writedSize *= 1.0
//                Log.i("StorageUtils", "writeToDisk() >>>> progress: ${writedSize / totalSize.toFloat()}")
                listener?.onProgress((writedSize / totalSize.toFloat()) * 100)
            }
            listener?.onSuccess()
            outputStream.flush()
            return true
        }
        return false
    }

    interface StorageFileListener {
        fun onSuccess()
        fun onFailure(msg: String?)
        fun onBegin()
        fun onProgress(progress: Double)
    }
}