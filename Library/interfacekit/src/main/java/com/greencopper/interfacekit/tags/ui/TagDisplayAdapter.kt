package com.greencopper.interfacekit.tags.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.databinding.TagBinding
import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy

internal class TagDisplayAdapter(private val tagColor: TagColor) :
    ListAdapter<DisplayableTag, TagDisplayAdapter.TagViewHolder>(TagDisplayDiff()) {

    private val localizationService: LocalizationService by App.lazy()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = TagBinding.inflate(LayoutInflater.from(parent.context))

        binding.cvTag.strokeColor = tagColor.pill.border
        binding.cvTag.backgroundTintList = ColorStateList.valueOf(tagColor.pill.background)
        binding.tvTag.setTextColor(tagColor.pill.label)
        binding.tvTag.setFont(InterfaceKitTextStyle.tags.label)

        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal inner class TagViewHolder(private val binding: TagBinding) : ViewHolder(binding.root) {

        fun bind(tag: DisplayableTag) {
            binding.tvTag.text = localizationService.getString(tag.name)
        }
    }
}

private class TagDisplayDiff : DiffUtil.ItemCallback<DisplayableTag>() {

    override fun areContentsTheSame(oldItem: DisplayableTag, newItem: DisplayableTag): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: DisplayableTag, newItem: DisplayableTag): Boolean {
        return oldItem.name == newItem.name
    }
}
