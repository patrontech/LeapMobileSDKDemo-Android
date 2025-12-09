package com.greencopper.interfacekit.search.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.SearchItemTitleSubtitleBinding
import com.greencopper.interfacekit.search.logic.SearchEntry
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.interfacekit.ui.setTextOrGone
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import kotlinx.coroutines.CoroutineScope

internal class SearchAdapter(
    private val displayImages: Boolean,
    private val onTapAction: (SearchEntry.ViewData) -> Unit,
    private val lifecycleScope: CoroutineScope,
) : ListAdapter<SearchEntry.ViewData, GenericSearchViewHolder>(SearchAdapterDiffUtil()) {

    companion object {
        private const val VIEW_TYPE_TITLE_SUBTITLE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SearchEntry.ViewData.TitleSubtitle -> VIEW_TYPE_TITLE_SUBTITLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericSearchViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_TITLE_SUBTITLE -> {
                val binding = SearchItemTitleSubtitleBinding.inflate(inflater, parent, false)
                TitleSubtitleViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: GenericSearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: GenericSearchViewHolder) {
        holder.cancelAllJobs()
        super.onViewRecycled(holder)
    }

    inner class TitleSubtitleViewHolder(
        override val binding: SearchItemTitleSubtitleBinding,
    ) : GenericSearchViewHolder.TypedSearchViewHolder<SearchEntry.ViewData.TitleSubtitle>(binding) {

        init {
            val color = InterfaceKitColor.search.titleSubtitleCell
            with(binding) {
                root.background = ColorDrawable(Color.WHITE).apply {
                    setTintList(color.background.toColorStateList())
                }
                searchItemTitle.setTextColor(color.name)
                searchItemTitle.setFont(InterfaceKitTextStyle.search.titleSubtitleCell.name)
                searchItemSubtitle.setTextColor(color.subtitle)
                searchItemSubtitle.setFont(InterfaceKitTextStyle.search.titleSubtitleCell.subtitle)
                cardView.strokeColor = color.image.stroke
                cardView.isVisible = displayImages
            }
        }

        override fun typedBind(data: SearchEntry.ViewData.TitleSubtitle) {
            with(binding) {
                root.setOnSafeClickListener { onTapAction(data) }
                searchItemTitle.text = data.title
                searchItemSubtitle.setTextOrGone(data.subtitle)
                searchItemIv.setImageResource(android.R.color.transparent)
                jobs.add(searchItemIv.setImageFrom(data.image, lifecycleScope))
            }
        }
    }
}

internal sealed class GenericSearchViewHolder(open val binding: ViewBinding) : JobAwareViewHolder(binding.root) {
    abstract fun bind(data: SearchEntry.ViewData)

    internal sealed class TypedSearchViewHolder<D : SearchEntry.ViewData>(binding: ViewBinding) :
        GenericSearchViewHolder(binding) {
        override fun bind(data: SearchEntry.ViewData) {
            typedBind(data as D)
        }

        abstract fun typedBind(data: D)
    }
}

private class SearchAdapterDiffUtil : DiffUtil.ItemCallback<SearchEntry.ViewData>() {

    override fun areItemsTheSame(oldItem: SearchEntry.ViewData, newItem: SearchEntry.ViewData): Boolean {
        return when {
            oldItem is SearchEntry.ViewData.TitleSubtitle && newItem is SearchEntry.ViewData.TitleSubtitle ->
                oldItem.title == newItem.title
                        && oldItem.subtitle == newItem.subtitle
                        && oldItem.image == newItem.image
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: SearchEntry.ViewData, newItem: SearchEntry.ViewData): Boolean {
        return when {
            oldItem is SearchEntry.ViewData.TitleSubtitle && newItem is SearchEntry.ViewData.TitleSubtitle ->
                oldItem.title == newItem.title
                        && oldItem.subtitle == newItem.subtitle
                        && oldItem.image == newItem.image
                        && oldItem.routeLink == newItem.routeLink
            else -> false
        }
    }
}


