package com.audio.example.dialog

import android.app.Activity
import com.audio.example.R
import com.audio.example.core.base.BaseDialog
import com.audio.example.core.extensions.setOnSingleClick
import com.audio.example.core.helper.StringHelper.changeGradientText
import com.audio.example.databinding.DialogDeleteBinding

class DeleteDialog(context: Activity) : BaseDialog<DialogDeleteBinding>(context) {
    var onClick: (() -> Unit)? = null
    override val layoutId: Int
        get() = R.layout.dialog_delete
    override val isCancelOnTouchOutside: Boolean
        get() = false
    override val isCancelableByBack: Boolean
        get() = false

    override fun initView() {
        changeGradientText(binding.tvCancel)
        binding.tvYes.setOnSingleClick {
            onClick?.invoke()
            dismiss()
        }
        binding.tvCancel.setOnSingleClick { dismiss() }
    }

    override fun initAction() {

    }

    override fun onDismissListener() {

    }
}