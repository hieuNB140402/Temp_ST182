package com.audio.example.dialog

import android.app.Activity
import com.audio.example.R
import com.audio.example.core.base.BaseDialog
import com.audio.example.core.extensions.setOnSingleClick
import com.audio.example.core.extensions.setOnSingleClickWithSound
import com.audio.example.databinding.DialogConfirmBinding
import kotlin.apply


class ConfirmDialog(
    val context: Activity, val title: Int, val description: Int
) : BaseDialog<DialogConfirmBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: (() -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    override fun initView() {
        initText()
    }

    override fun initAction() {
        binding.apply {
            btnNo.setOnSingleClickWithSound {
                onNoClick.invoke()
            }
            btnYes.setOnSingleClickWithSound {
                onYesClick.invoke()
            }
            flOutSide.setOnSingleClick {
                onDismissClick.invoke()
            }
        }
    }

    override fun onDismissListener() {

    }

    private fun initText() {
        binding.apply {
            tvTitle.text = context.getString(title)
            tvDescription.text = context.getString(description)
        }
    }
}