package com.greencopper.interfacekit.tags.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.greencopper.interfacekit.databinding.TagDisplayBinding
import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.interfacekit.ui.HorizontalSpacingItemDecorator
import com.greencopper.interfacekit.ui.dpToPx

public class TagDisplayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val binding = TagDisplayBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var adapter: TagDisplayAdapter

    init {
        binding.rvTagDisplay.addItemDecoration(HorizontalSpacingItemDecorator(spacing = 8.dpToPx()))
    }

    public fun setup(color: TagColor) {
        adapter = TagDisplayAdapter(color)
        binding.rvTagDisplay.adapter = adapter
    }

    public fun setTags(tags: List<DisplayableTag>) {
        adapter.submitList(tags)
    }
}
