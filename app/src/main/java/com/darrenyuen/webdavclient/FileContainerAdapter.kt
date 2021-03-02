package com.darrenyuen.webdavclient

import android.annotation.SuppressLint
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
class FileContainerAdapter(private val context: Context, private var fileList: List<FileBean>) : RecyclerView.Adapter<FileContainerAdapter.ViewHolder>() {

    val TAG = "FileContainerAdapter"

    private var mOnItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        mOnItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_file, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i(TAG, fileList[position].path)
        var fileType = FileType.File
        if (fileList[position].name.contains(".txt")) {
            holder.iconFileTypeIV.setImageResource(R.drawable.txt)
        } else if (fileList[position].path.contains(".pdf")) {
            holder.iconFileTypeIV.setImageResource(R.drawable.pdf)
        } else if (fileList[position].path.contains(".apk")) {
            holder.iconFileTypeIV.setImageResource(R.drawable.apk)
        } else {
            holder.iconFileTypeIV.setImageResource(R.drawable.dir)
//            holder.fileSizeTV.visibility = View.GONE
            fileType = FileType.Dir
        }
        holder.fileNameTV.text = fileList[position].name
        if (fileType == FileType.File) {
            holder.fileSizeTV.text = fileList[position].size.toString().substring(0, fileList[position].size.toString().indexOf(".") + 2) + "MB"
        } else if (fileType == FileType.Dir) holder.fileSizeTV.visibility = View.GONE
        holder.fileTimeTV.text = fileList[position].lastModified.toString().replace("GMT+08:00 ", "")
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClick(fileList[position].path, fileType)
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun setData(data: List<FileBean>) {
        fileList = data
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconFileTypeIV: ImageView = itemView.findViewById(R.id.icon_file_type)
        val iconDoneIV = itemView.findViewById<ImageView>(R.id.icon_done)
        val fileNameTV = itemView.findViewById<TextView>(R.id.fileName)
        val fileSizeTV = itemView.findViewById<TextView>(R.id.fileSize)
        val fileTimeTV = itemView.findViewById<TextView>(R.id.time)
    }

    interface OnItemClickListener {
        fun onItemClick(path: String, type: FileType)
    }

}