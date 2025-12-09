package com.greencopper.interfacekit.widgets.ui.linkscollectionwidget

import android.graphics.PorterDuff
import android.view.*
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.LinksCollectionItemLayoutBinding
import com.greencopper.interfacekit.imageservice.ImageResult
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.setShadowColor
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

internal class LinksCollectionAdapter(
    private val lifecycleScope: CoroutineScope,
    private val onItemSelected: (LinkItem) -> Unit,
) : ListAdapter<LinksCollectionAdapter.LinkItem, LinksCollectionAdapter.LinkItemViewHolder>(
    LinksCollectionAdapterDiffUtil()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkItemViewHolder =
        LinkItemViewHolder(
            LinksCollectionItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemSelected
        )

    override fun onBindViewHolder(holder: LinkItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    override fun onViewRecycled(holder: LinkItemViewHolder) {
        holder.cancelAllJobs()
        super.onViewRecycled(holder)
    }

    inner class LinkItemViewHolder(
        val binding: LinksCollectionItemLayoutBinding,
        val onItemSelected: (LinkItem) -> Unit,
    ) : JobAwareViewHolder(binding.root) {

        private val imageService: ImageService by App.lazy()
        private val localizationService: LocalizationService by App.lazy()
        private val colors = InterfaceKitColor.linksCollectionWidget.link

        fun bind(item: LinkItem) {
            binding.root.contentDescription = localizationService.getString(item.accessibilityLabel)
            with(binding.linkItemLabel) {
                text = item.text?.let { localizationService.getString(it) }
                setTextColor(colors.text)
                setFont(InterfaceKitTextStyle.linksCollectionWidget.link.text)
                if(item.hideLabel) visibility = View.GONE
            }

            with(binding.linkItemButton) {
                setButtonColors()
                clearColorFilter()
                setImageResource(android.R.color.transparent)
                jobs.add(
                    imageService.getImageDrawable(
                        item.icon,
                        hideIfUnknown = true,
                        hideIfLoading = true,
                    )
                        .flowOn(Dispatchers.IO)
                        .onEach { result ->
                            if (item.shouldColor && result is ImageResult.READY) {
                                result.drawable?.let { setColorFilter(colors.button.icon, PorterDuff.Mode.SRC_ATOP) }
                            }
                            setImageDrawable(result.drawable)
                        }.launchIn(lifecycleScope)
                )

                setOnSafeClickListener {
                    onItemSelected(item)
                    item.onTapAnalytics?.let {
                        App.track(it.copy())
                    }
                }
            }

        }

        private fun ImageButton.setButtonColors() {
            backgroundTintList = colors.button.background.toColorStateList()
            setShadowColor(colors.button.shadow)
        }
    }

    data class LinkItem(
        val text: String? = null,
        val icon: String,
        val shouldColor: Boolean,
        val onTap: String,
        val onTapAnalytics: WidgetEventAnalytics? = null,
        val hideLabel: Boolean,
        val accessibilityLabel: String? = null,
    )

    private class LinksCollectionAdapterDiffUtil : DiffUtil.ItemCallback<LinkItem>() {

        override fun areContentsTheSame(oldItem: LinkItem, newItem: LinkItem): Boolean =
            oldItem == newItem

        override fun areItemsTheSame(oldItem: LinkItem, newItem: LinkItem): Boolean =
            oldItem == newItem
    }
}
