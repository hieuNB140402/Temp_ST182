package com.audio.example.ui.my_record

import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.audio.example.R
import com.audio.example.core.base.AbsBaseAdapter
import com.audio.example.core.base.AbsBaseDiffCallBack
import com.audio.example.core.extensions.setOnSingleClick
import com.audio.example.core.helper.UnitHelper.dpToPx
import com.audio.example.databinding.ItemMyRecordBinding
import com.audio.example.databinding.PopupMyRecordBinding
import java.io.File

class MyRecordAdapter :
    AbsBaseAdapter<String, ItemMyRecordBinding>(R.layout.item_my_record, DiffCallBack()) {
    companion object {
        var TYPE_PLAY = "TYPE_PLAY"
        var TYPE_REVERSE = "TYPE_REVERSE"
        var TYPE_SHARE = "TYPE_SHARE"
        var TYPE_RENAME = "TYPE_RENAME"
        var TYPE_DOWNLOAD = "TYPE_DOWNLOAD"
        var TYPE_DELETE = "TYPE_DELETE"
    }

    var onClick: ((Int, String) -> Unit)? = null
    var checkForcus = -1
    var checkPlay = 0 //0-play, 1-reverse
    val location = IntArray(2)
    var anchorY = 0
    var screenHeight: Int = 0
    lateinit var displayFrame: Rect
    var availableHeightBelow: Int = 0
    override fun bind(
        binding: ItemMyRecordBinding,
        position: Int,
        data: String,
        holder: RecyclerView.ViewHolder
    ) {
        binding.apply {
            if (position == checkForcus) {
                if (checkPlay == 0) {
                    binding.imvReverse.setImageResource(R.drawable.imv_reverse_my_record)
                    binding.imvPlay.setImageResource(R.drawable.imv_stop_my_record)

                } else {
                    binding.imvPlay.setImageResource(R.drawable.imv_play_my_record)
                    binding.imvReverse.setImageResource(R.drawable.imv_stop_my_record)
                }
            } else {
                binding.imvReverse.setImageResource(R.drawable.imv_reverse_my_record)
                binding.imvPlay.setImageResource(R.drawable.imv_play_my_record)
            }
            tvSong.text = File(data).name
            tvSong.isSelected = true
            llPlay.setOnSingleClick {
                onClick?.invoke(position, TYPE_PLAY)
            }
            llReverse.setOnSingleClick {
                onClick?.invoke(position, TYPE_REVERSE)
            }
            llShare.setOnSingleClick {
                onClick?.invoke(position, TYPE_SHARE)
            }
            val bindingPopup =
                PopupMyRecordBinding.inflate(LayoutInflater.from(binding.root.context))
            val popupMyAlbumBinding = PopupWindow(
                bindingPopup.root,
                dpToPx(binding.root.context, 162).toInt(),
                WRAP_CONTENT,
                true
            )
            imvDot.setOnSingleClick {
                it.getLocationOnScreen(location)
                anchorY = location[1]
                screenHeight = binding.root.resources.displayMetrics.heightPixels
                displayFrame = Rect()
                it.getWindowVisibleDisplayFrame(displayFrame)
                availableHeightBelow = screenHeight - anchorY
                if (availableHeightBelow > dpToPx(binding.root.context, 180f)) {
                    popupMyAlbumBinding.showAsDropDown(
                        it,
                        -(dpToPx(binding.root.context, 140f).toInt()),
                        -(dpToPx(binding.root.context, 0f).toInt()),
                        Gravity.CENTER
                    )
                } else {
                    popupMyAlbumBinding.showAsDropDown(
                        it,
                        -(dpToPx(binding.root.context, 140f).toInt()),
                        -(dpToPx(binding.root.context, 180f).toInt()),
                        Gravity.CENTER
                    )
                }
                bindingPopup.tv1.isSelected = true
                bindingPopup.tv2.isSelected = true
                bindingPopup.tv3.isSelected = true
                bindingPopup.llDownload.setOnSingleClick {
                    onClick?.invoke(position, TYPE_DOWNLOAD)
                    popupMyAlbumBinding.dismiss()
                }
                bindingPopup.llRename.setOnSingleClick {
                    onClick?.invoke(position, TYPE_RENAME)
                    popupMyAlbumBinding.dismiss()
                }
                bindingPopup.llDelete.setOnSingleClick {
                    onClick?.invoke(position, TYPE_DELETE)
                    popupMyAlbumBinding.dismiss()
                }
            }
        }
    }

    class DiffCallBack : AbsBaseDiffCallBack<String>() {
        override fun itemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun contentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem != newItem
        }

    }
}