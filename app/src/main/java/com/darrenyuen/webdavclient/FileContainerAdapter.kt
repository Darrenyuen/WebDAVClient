package com.darrenyuen.webdavclient

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.darrenyuen.webdavclient.widget.BottomDialog
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine

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
        if (mNode.mChildren[position].mValue.name.contains(".txt")) {
            holder.iconFileTypeIV.setImageResource(R.drawable.txt)
        } else if (mNode.mChildren[position].mValue.name.contains(".pdf")) {
            holder.iconFileTypeIV.setImageResource(R.drawable.pdf)
        } else if (mNode.mChildren[position].mValue.name.contains(".apk")) {
            holder.iconFileTypeIV.setImageResource(R.drawable.apk)
        } else if (mNode.mChildren[position].mValue.name.contains(".doc")) {
            holder.iconFileTypeIV.setImageResource(R.drawable.word)
        } else {
            holder.iconFileTypeIV.setImageResource(R.drawable.dir)
//            holder.fileSizeTV.visibility = View.GONE
            fileType = FileType.Dir
        }
        holder.fileNameTV.text = mNode.mChildren[position].mValue.name
        if (fileType == FileType.File) {
            holder.fileSizeTV.text = mNode.mChildren[position].mValue.size.toString().substring(0, mNode.mChildren[position].mValue.size.toString().indexOf(".") + 2) + "MB"
        } else if (fileType == FileType.Dir) holder.fileSizeTV.visibility = View.GONE
        holder.fileTimeTV.text = mNode.mChildren[position].mValue.lastModified.toString().replace("GMT+08:00 ", "")
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClick(mNode.mChildren[position], fileType)
        }
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
                            R.id.rename -> webDavOperation(WebDavOperation.Rename, mNode.mChildren[position].mValue.path)
                            R.id.copy -> webDavOperation(WebDavOperation.Copy, mNode.mChildren[position].mValue.path)
                            R.id.detail -> webDavOperation(WebDavOperation.Detail, mNode.mChildren[position].mValue.path)
                            R.id.delete -> webDavOperation(WebDavOperation.Delete, mNode.mChildren[position].mValue.path)
                        }
                    }
                    .build()
            mBottomDialog?.show()
        }
    }

    private fun webDavOperation(operation: WebDavOperation, path: String) {
        when(operation) {
//            WebDavOperation.Rename -> {
//                Thread {
//                    val sardine = OkHttpSardine()
//                    sardine.setCredentials("dev", "yuan")
//                    Log.i(TAG, "http://119.29.176.115$path")
//                    sardine.
//                }.start()
            WebDavOperation.Delete -> {
                Thread {
                    val sardine = OkHttpSardine()
                    sardine.setCredentials("dev", "yuan")
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