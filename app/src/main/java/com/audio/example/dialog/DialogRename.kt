package com.audio.example.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.audio.example.R
import com.audio.example.core.base.BaseDialog
import com.audio.example.core.extensions.setOnSingleClick
import com.audio.example.core.extensions.showToast
import com.audio.example.core.helper.StringHelper.changeGradientText
import com.audio.example.databinding.DialogRenameBinding
import java.io.File

class DialogRename(context: Activity,var path: String) : BaseDialog<DialogRenameBinding>(context) {
    var onClick: ((String) -> Unit)? = null
    override val layoutId: Int
        get() = R.layout.dialog_rename
    override val isCancelOnTouchOutside: Boolean
        get() = false
    override val isCancelableByBack: Boolean
        get() = false

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        changeGradientText(binding.tvCancel)
        binding.edtRename.setText(File(path).name)
        binding.tvSave.setOnSingleClick {
            if(binding.edtRename.text.toString().trim()==""){
                Toast.makeText(context, ContextCompat.getString(context, R.string.file_name_cannot_be_blank), Toast.LENGTH_SHORT).show()
            }else{
                onClick?.invoke(binding.edtRename.text.toString().trim().removeSuffix(".wav"))
                dismiss()
            }

        }
        binding.tvCancel.setOnSingleClick { dismiss() }
        binding.edtRename.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.edtRename.compoundDrawablesRelative[2]
                    ?: return@setOnTouchListener false
                if (event.x >= (binding.edtRename.width - binding.edtRename.paddingEnd - drawableEnd.bounds.width())) {
                    binding.edtRename.text?.clear()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    override fun initAction() {

    }

    override fun onDismissListener() {

    }
}