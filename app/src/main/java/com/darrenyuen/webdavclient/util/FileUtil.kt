package com.darrenyuen.webdavclient.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
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
    fun openFile(path: String, context: Context): Intent? {
        val file = File(path)
        if (!file.exists()) return null
        if (!file.name.contains(".")) return getOtherFileIntent()
        val suffix = file.name.substringAfterLast(".").toLowerCase(Locale.ROOT)
        Log.d("open file", suffix)
        return when(suffix) {
            "m4a", "mp3", "mid", "xmf", "ogg", "wav" -> getAudioFileIntent(path, context)
            "mp4", "3gp" -> getVideoFileIntent(path, context)
            "jpg", "gif", "png", "jpeg", "bmp" -> getImageFileIntent(path, context)
            "ppt" -> getPPTFileIntent(path, context)
            "xls", "xlsx", "xlsx_tmp" -> getExcelFileIntent(path, context)
            "doc", "docx" -> getWordFileIntent(path, context)
            "txt" -> getTextFileIntent(path, context)
            "pdf" -> getPDFFileIntent(path, context)
            "apk" -> getApkFileIntent(path, context)
            else -> getOtherFileIntent()
        }
    }

    private fun getAudioFileIntent(param: String, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("oneshot", 0)
        intent.putExtra("configchange", 0)
        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "audio/*")
        return intent
    }

    private fun getVideoFileIntent(param: String, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("oneshot", 0)
        intent.putExtra("configchange", 0)
        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "video/*")
        return intent
    }

    private fun getImageFileIntent(param: String, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "image/*")
        return intent
    }

    private fun getPPTFileIntent(param: String, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        return intent
    }

    private fun getExcelFileIntent(param: String, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/vnd.ms-excel")
        return intent
    }

    private fun getWordFileIntent(param: String, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/msword")
        return intent
    }

    private fun getTextFileIntent(param: String, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "text/plain")
        return intent
    }

    private fun getPDFFileIntent(param: String, context: Context): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/pdf")
        return intent
    }

    private fun getApkFileIntent(param: String, context: Context): Intent {
        val intent = Intent("android.content.Intent.ACTION_VIEW")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = FileProvider.getUriForFile(context, "webdavclient.fileprovider", File(param))
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        return intent
    }

    private fun getOtherFileIntent(): Intent {
        return Intent().putExtra("other", "other")
    }

}