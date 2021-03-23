package com.darrenyuen.webdavclient

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class InputDialogFragment : DialogFragment() {

    private var callback: Callback? = null

    private var mOldName: String? = null
    private var mPath: String? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is Callback) {
            this.callback = activity
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_input_dialog, null)
        return AlertDialog.Builder(activity!!)
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

    fun show(manager: FragmentManager, tag: String?, oldName: String, path: String) {
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
    }
}