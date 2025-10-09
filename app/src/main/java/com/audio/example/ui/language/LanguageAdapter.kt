package com.audio.example.ui.language

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.audio.example.R
import com.audio.example.core.base.BaseAdapter
import com.audio.example.core.extensions.gone
import com.audio.example.core.extensions.loadImageGlide
import com.audio.example.core.extensions.setBackgroundConnerSmooth
import com.audio.example.core.extensions.setFont
import com.audio.example.core.extensions.setOnSingleClickWithSound
import com.audio.example.core.extensions.visible
import com.audio.example.data.model.LanguageModel
import com.audio.example.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val context: Context
) : ListAdapter<LanguageModel, LanguageAdapter.LanguageVH>(object :
    DiffUtil.ItemCallback<LanguageModel>() {
    override fun areItemsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean {
        // So sánh theo mã ngôn ngữ
        return oldItem.code == newItem.code
    }

    override fun areContentsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean {
        // So sánh toàn bộ nội dung
        return oldItem == newItem
    }
}) {

    var onItemClick: ((String) -> Unit)? = null

    inner class LanguageVH(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LanguageModel) {
            binding.apply {
                // Load ảnh cờ
                loadImageGlide(root, item.flag, imvFlag, false)
                tvLang.text = item.name

                // Hiển thị trạng thái active
                if (item.activate) {
                    loadImageGlide(root, R.drawable.ic_tick_lang, rdbLang, false)
                    tvLang.setTextColor(context.getColor(R.color.white))
                    layoutBg.setBackgroundResource(R.drawable.bg_10_solid_gradient)
                    itemLang.cardElevation = 6f

                } else {
                    loadImageGlide(root, R.drawable.ic_not_tick_lang, rdbLang, false)
                    tvLang.setTextColor(context.getColor(R.color.black_CC_opacity))
                    layoutBg.setBackgroundResource(R.drawable.bg_10_stroke_gradient_solid_white)
                    itemLang.cardElevation = 0f
                }

                // Xử lý click
                root.setOnSingleClickWithSound {
                    onItemClick?.invoke(item.code)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLanguageBinding.inflate(inflater, parent, false)
        return LanguageVH(binding)
    }

    override fun onBindViewHolder(holder: LanguageVH, position: Int) {
        holder.bind(getItem(position))
    }

    // Cập nhật trạng thái active
    fun submitItem(position: Int) {
        val currentList = currentList.mapIndexed { index, item ->
            item.copy(activate = index == position)
        }
        submitList(currentList)
    }

}
