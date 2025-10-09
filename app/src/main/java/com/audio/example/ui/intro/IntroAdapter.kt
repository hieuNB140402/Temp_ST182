package com.audio.example.ui.intro

import android.content.Context
import com.bumptech.glide.Glide
import com.audio.example.R
import com.audio.example.core.base.BaseAdapter
import com.audio.example.core.extensions.loadImageGlide
import com.audio.example.core.extensions.select
import com.audio.example.core.extensions.setTextContent
import com.audio.example.data.model.IntroModel
import com.audio.example.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImageGlide(root, item.image, imvImage, false)
            tvContent.setTextContent(context, item.content)
            tvContent.select()
        }
    }
}