package com.audio.example.dialog

import android.app.Activity
import com.audio.example.R
import com.audio.example.core.base.BaseDialog
import com.audio.example.core.extensions.setBackgroundConnerSmooth
import com.audio.example.databinding.DialogLoadingBinding

class LoadingDialog(val context: Activity) :
    BaseDialog<DialogLoadingBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_loading
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    override fun initView() {
    }

    override fun initAction() {}

    override fun onDismissListener() {}

}