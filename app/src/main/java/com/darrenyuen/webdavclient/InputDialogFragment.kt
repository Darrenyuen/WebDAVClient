package com.darrenyuen.webdavclient

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class InputDialogFragment : DialogFragment() {

    private var callback: Callback? = null

    private var mOldName: String? = null
    private var mPath: String? = null

    companion object {
        const val OP = "OPERATION"
        const val RENAME_OP = "RENAME"
        const val CREATE_DIR_OP = "CREATE_DIR"
        const val CREATE_FILE_OP = "CREATE_FILE"
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is Callback) {
            this.callback = activity
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_input_dialog, null)
        return when (arguments?.get(OP)) {
            RENAME_OP -> {
                AlertDialog.Builder(activity!!)
                        .setTitle("重命名")
                        .setView(view)
                        .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->
                            callback?.let {
                                val fileNameEditText = view.findViewById<EditText>(R.id.fileNameEditText)
                                if (fileNameEditText.text != null && fileNameEditText.text.isNotEmpty()) {
                                    it.onClickForRename(fileNameEditText.text.toString(), mOldName!!, mPath!!)
                                }

                            }
                        }
                        .setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->

                        }
                        .create()
            }
            CREATE_DIR_OP -> {
                AlertDialog.Builder(activity!!)
                        .setTitle("请输入文件夹名称:")
                        .setView(view)
                        .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->
                            callback?.let {
                                val fileNameEditText = view.findViewById<EditText>(R.id.fileNameEditText)
                                if (fileNameEditText.text != null && fileNameEditText.text.isNotEmpty()) {
                                    it.onClickForCreateDir(mPath!!, fileNameEditText.text.toString())
                                }

                            }
                        }
                        .setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->

                        }
                        .create()
            }
            else -> {
                AlertDialog.Builder(activity!!)
                        .setTitle("请输入文件名(带上文件格式):")
                        .setView(view)
                        .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->
                            callback?.let {
                                val fileNameEditText = view.findViewById<EditText>(R.id.fileNameEditText)
                                if (fileNameEditText.text != null && fileNameEditText.text.isNotEmpty()) {
                                    it.onClickForCreateFile(mPath!!, fileNameEditText.text.toString())
                                }

                            }
                        }
                        .setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->

                        }
                        .create()
            }
        }
    }

    fun show(manager: FragmentManager, tag: String?, path: String, oldName: String = "") {
        super.show(manager, tag)
        mOldName = oldName
        mPath = path
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback = null
    }

    interface Callback {
        fun onClickForRename(newName: String, oldName: String, path: String)
        fun onClickForCreateDir(path: String, dirName: String)
        fun onClickForCreateFile(path: String, fileName: String)
    }
}