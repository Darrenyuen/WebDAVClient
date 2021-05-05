package com.darrenyuen.webdavclient.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.sun.xml.fastinfoset.util.StringArray
import java.io.File
import java.lang.Exception
import java.net.URI
import java.util.*

/**
 * Create by yuan on 2021/3/2
 */
object FileUtil {

    //获取上级或下级文件列表
    fun getGroupFiles(path: String): MutableList<String> {
        val file = File(path)
        val list = mutableListOf<String>()
        file.listFiles()?.forEach { it ->
            list.add(it.absolutePath)
        }
        return list
    }

    //打开各类型的文件
    fun openFile(uri: Uri, fileName: String, context: Context): Intent? {
//        val file = File(path)
//        if (!file.exists()) return null
        if (!fileName.contains(".")) return getOtherFileIntent()
        val suffix = fileName.substringAfterLast(".").toLowerCase(Locale.ROOT)
        Log.d("open file", suffix)
        return when(suffix) {
            "m4a", "mp3", "mid", "xmf", "ogg", "wav" -> getAudioFileIntent(uri, context)
            "mp4", "3gp" -> getVideoFileIntent(uri, context)
            "jpg", "gif", "png", "jpeg", "bmp" -> getImageFileIntent(uri, context)
            "ppt" -> getPPTFileIntent(uri, context)
            "xls", "xlsx", "xlsx_tmp" -> getExcelFileIntent(uri, context)
            "doc", "docx" -> getWordFileIntent(uri, context)
            "txt" -> getTextFileIntent(uri, context)
            "pdf" -> getPDFFileIntent(uri, context)
            "apk" -> getApkFileIntent(uri, context)
            else -> getOtherFileIntent()
        }
    }

    private fun getAudioFileIntent(uri: Uri, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("oneshot", 0)
        intent.putExtra("configchange", 0)
//        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "audio/*")
        return intent
    }

    private fun getVideoFileIntent(uri: Uri, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("oneshot", 0)
        intent.putExtra("configchange", 0)
//        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "video/*")
        return intent
    }

    private fun getImageFileIntent(uri: Uri, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "image/*")
        return intent
    }

    private fun getPPTFileIntent(uri: Uri, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        return intent
    }

    private fun getExcelFileIntent(uri: Uri, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/vnd.ms-excel")
        return intent
    }

    private fun getWordFileIntent(uri: Uri, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/msword")
        return intent
    }

    private fun getTextFileIntent(uri: Uri, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "text/plain")
        return intent
    }

    private fun getPDFFileIntent(uri: Uri, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/pdf")
        return intent
    }

    private fun getApkFileIntent(uri: Uri, context: Context): Intent {
        val intent = Intent("android.content.Intent.ACTION_VIEW")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        return intent
    }

    private fun getOtherFileIntent(): Intent {
        return Intent().putExtra("other", "other")
    }

    fun isExist(context: Context, fileName: String, fileType: StorageUtils.FileType): Boolean {
        var uri = when (fileType) {
            StorageUtils.FileType.Photo -> {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                }
//                context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            }
            StorageUtils.FileType.Video -> {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                }
//                context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return false

            }
            StorageUtils.FileType.File -> {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                }
//                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return false
            }
        }
//        uri = uri ?: Uri.parse("")
//        context.contentResolver.
//        try {
//        if (getRealFilePath(context, uri) == null) return false
//        Log.i("ASDFASFSDA", "file is exist: ${File(getRealFilePath(context, uri)!!).exists()}")
//        return File(getRealFilePath(context, uri)!!).exists()
        return true
    }

    private fun getRealFilePath(context: Context, uri: Uri?): String? {
        if (uri == null) return null
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) {
            data = uri.path
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            val cursor = context.getContentResolver ().query(uri!!, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    var index = cursor.getColumnIndex (MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data
    }

}