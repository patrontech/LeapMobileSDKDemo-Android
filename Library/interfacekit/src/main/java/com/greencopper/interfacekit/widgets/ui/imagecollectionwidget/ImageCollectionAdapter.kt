package com.greencopper.interfacekit.widgets.ui.imagecollectionwidget

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.greencopper.core.asset.recipe.Asset
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.ImageCollectionItemLayoutBinding
import com.greencopper.interfacekit.imageservice.ImageResult
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy

internal class ImageCollectionAdapter(
    private val optimalItemWidth: Int,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val onItemSelected: (ImageItem) -> Unit,
) : ListAdapter<ImageCollectionAdapter.ImageItem, ImageCollectionAdapter.ImageItemViewHolder>(
    ImageCollectionAdapterDiffUtil()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder =
        ImageItemViewHolder(
            ImageCollectionItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                root.updateLayoutParams {
                    width = optimalItemWidth
                }
            },
            onItemSelected
        )

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) =
        holder.bind(getItem(position), lifecycleScope)

    override fun onViewRecycled(holder: ImageItemViewHolder) {
        holder.cancelAllJobs()
        super.onViewRecycled(holder)
    }

    internal class ImageItemViewHolder(
        val binding: ImageCollectionItemLayoutBinding,
        val onItemSelected: (ImageItem) -> Unit,
    ) : JobAwareViewHolder(binding.root) {

        init {
            val colors = InterfaceKitColor.imageCollectionWidget.item
            binding.imageCollectionItemLabel.setTextColor(colors.label)
            binding.imageCollectionItemLabel.setFont(InterfaceKitTextStyle.imageCollectionWidget.image.label)
            binding.imageCollectionItemImage.setShadowColor(colors.shadow)
            binding.imageCollectionItemImage.setBackgroundColor(colors.background)
        }

        private val localizationService: LocalizationService by App.lazy()

        fun bind(item: ImageItem, lifecycleScope: LifecycleCoroutineScope) {
            binding.imageCollectionItemLabel.setOtaTextOrGone(localizationService, item.label)
            with(binding.imageCollectionItemImage) {
                resetImageFrom(item.image, lifecycleScope, format = Asset.Format.Name.THUMBNAIL) {
                    if (it is ImageResult.LOADING) {
                        binding.cardView.setCardBackgroundColor(Color.TRANSPARENT)
                        binding.imageCollectionItemImage.setBackgroundResource(android.R.color.transparent)
                    }
                    it.drawable
                }?.also {
                    jobs.add(it)
                }
            }

            with(binding.root) {
                setOnTouchListener(OnTouchClickListener(
                    context,
                    onTouchInternal = { _, event ->
                        playScalingAnimationOnEvent(event, this)
                        false
                    },
                    onClick = {
                        onItemSelected(item)
                        item.onTapAnalytics?.let {
                            App.track(it.copy())
                        }
                    }
                ))
                contentDescription = localizationService.getString(item.accessibilityName)
            }
        }
    }

    data class ImageItem(
        val image: String,
        val label: String? = null,
        val accessibilityName: String? = null,
        val onTapRouteLink: String,
        val onTapAnalytics: WidgetEventAnalytics? = null,
    )

    private class ImageCollectionAdapterDiffUtil : DiffUtil.ItemCallback<ImageItem>() {

        override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean =
            oldItem == newItem

        override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean =
            oldItem == newItem
    }
}
