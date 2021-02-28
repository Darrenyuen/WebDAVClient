package com.darrenyuen.webdavclient

import android.content.Context
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by yuan on 2021/2/28
 */
class FileContainerAdapter(private val context: Context, val fileList: List<String>) : RecyclerView.Adapter<FileContainerAdapter.ViewHolder>() {

    val TAG = "FileContainerAdapter"

    var numOfInvalid = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_file, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!fileList[position].contains(".")) {
            numOfInvalid++
            return
        }
        Log.i(TAG, fileList[position].substring(fileList[position].indexOf(".")))
        when(fileList[position].substring(fileList[position].indexOf("."))) {
            ".txt" -> holder.iconFileTypeIV.setImageResource(R.drawable.txt)
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconFileTypeIV = itemView.findViewById<ImageView>(R.id.icon_file_type)
        val iconDoneIV = itemView.findViewById<ImageView>(R.id.icon_done)
        val fileNameTV = itemView.findViewById<TextView>(R.id.fileName)
        val fileSizeTV = itemView.findViewById<TextView>(R.id.fileSize)
        val fileTimeTV = itemView.findViewById<TextView>(R.id.time)
    }
}