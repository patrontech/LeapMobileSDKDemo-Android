package com.greencopper.event.activity.ui.activitylist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.greencopper.core.asset.recipe.Asset.Format.Name.THUMBNAIL
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.track
import com.greencopper.event.activity.ContentActivity
import com.greencopper.event.activity.ui.AddToMyActivitiesAnalytics
import com.greencopper.event.activity.ui.RemoveFromMyActivitiesAnalytics
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.ActivityItemBinding
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.WidgetCollectionCellBinding
import com.greencopper.interfacekit.favorites.*
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCell
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.toolkit.App
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

internal class ActivitiesListAdapter(
    private val origin: Layout,
    private val screenName: String,
    private val displayImages: Boolean,
    private val favoriteIcons: FavoriteIcons? = null,
    private val widgetCollectionCellSeparator: BottomDrawableItemDecorator.DecoratorInfos,
    private val myActivitiesManager: FavoritesManager<Long>,
    private val onActivityItemClicked: (ActivitiesListItem.Card) -> Unit,
) : ListAdapter<ActivitiesListItem, JobAwareViewHolder>(ActivitiesListAdapterDiffUtil()) {

    private val inflater: LayoutInflater = origin.layoutInflater

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ActivitiesListItem.Card -> VIEW_TYPE_CARD
            is ActivitiesListItem.WidgetCollectionHolder -> WidgetCollectionCell.ADAPTER_TYPE
        }
    }

    internal inner class ActivitiesViewHolder(
        private val binding: ActivityItemBinding,
    ) : JobAwareViewHolder(binding.root) {

        private val cellColors = EventColor.activitiesList.cell

        internal fun setupColors() {
            val textStyles = EventTextStyle.activitiesList.cell

            with(binding) {
                activityItemTitle.setTextColor(cellColors.name)
                activityItemTitle.setFont(textStyles.name)
                activityItemSubtitle.setTextColor(cellColors.subtitle)
                activityItemSubtitle.setFont(textStyles.subtitle)
                activityItemContainer.background = ColorDrawable(Color.WHITE).apply {
                    setTintList(cellColors.background.toColorStateList())
                }
            }
        }

        internal fun bind(data: ActivitiesListItem.Card) {
            with(binding) {
                cardView.isVisible = displayImages
                activityItemPicture.resetImageFrom(
                    data.photo,
                    origin.viewLifecycleOwner.lifecycleScope,
                    format = THUMBNAIL,
                )?.also { jobs.add(it) }

                activityItemTitle.text = data.name
                activityItemSubtitle.setTextOrGone(data.subtitle)
                activityItemContainer.setOnSafeClickListener { onActivityItemClicked(data) }
                favoriteIcons?.let {
                    bindFavoriteData(it, data)
                } ?: run {
                    binding.activityItemFavoriteAddRemove.isVisible = false
                }
            }
        }

        private fun bindFavoriteData(icons: FavoriteIcons, data: ActivitiesListItem.Card) {
            with(binding.activityItemFavoriteAddRemove) {
                isVisible = true
                if (data.isFavorite == true) {
                    contentDescription = icons.removeAccessibilityLabel
                    jobs.add(
                        setImageFrom(
                            icons.removeIcon,
                            origin.viewLifecycleOwner.lifecycleScope,
                            hideIfUnknown = true,
                            hideIfLoading = true,
                        )
                    )
                } else {
                    contentDescription = icons.addAccessibilityLabel
                    jobs.add(
                        setImageFrom(
                            icons.addIcon,
                            origin.viewLifecycleOwner.lifecycleScope,
                            hideIfUnknown = true,
                            hideIfLoading = true,
                        )
                    )
                }
                setColorFilter(cellColors.myActivityIcon)
                setOnSafeClickListener { updateFavorite(data) }
            }
        }

        private fun updateFavorite(item: ActivitiesListItem.Card) {
            if (item.isFavorite == true) {
                myActivitiesManager.removeFromFavorites(item)
                App.track(
                    RemoveFromMyActivitiesAnalytics(
                        screenName = screenName,
                        itemId = item.itemId,
                        itemName = item.name
                    )
                )
            } else {
                myActivitiesManager.addToFavorites(item)
                App.track(
                    AddToMyActivitiesAnalytics(
                        screenName = screenName,
                        itemId = item.itemId,
                        itemName = item.name
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobAwareViewHolder {
        return when (viewType) {
            VIEW_TYPE_CARD -> {
                ActivitiesViewHolder(ActivityItemBinding.inflate(inflater, parent, false)).also {
                    it.setupColors()
                }
            }

            WidgetCollectionCell.ADAPTER_TYPE -> {
                val widgetsView = WidgetCollectionCellBinding.inflate(inflater, parent, false)
                WidgetCollectionCell(widgetsView)
            }

            else -> throw IllegalStateException("${ActivitiesListAdapter::class.simpleName} not set up properly")
        }
    }

    override fun onBindViewHolder(holder: JobAwareViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ActivitiesViewHolder -> holder.bind(item as ActivitiesListItem.Card)
            is WidgetCollectionCell -> holder.bind(
                (item as ActivitiesListItem.WidgetCollectionHolder).widgets,
                origin,
                screenName,
                widgetCollectionCellSeparator.takeIf { currentList.lastIndex != position || it.showLast },
                position == 0,
                position == itemCount - 1,
            )
        }
    }

    override fun onViewRecycled(holder: JobAwareViewHolder) {
        holder.cancelAllJobs()
        super.onViewRecycled(holder)
    }

    companion object {
        private const val VIEW_TYPE_CARD = 1
    }
}

private class ActivitiesListAdapterDiffUtil : DiffUtil.ItemCallback<ActivitiesListItem>() {

    override fun areContentsTheSame(
        oldItem: ActivitiesListItem,
        newItem: ActivitiesListItem,
    ): Boolean {
        return if (oldItem is ActivitiesListItem.Card
            && newItem is ActivitiesListItem.Card
        ) {
            oldItem == newItem && !isFavoriteUpdate(oldItem, newItem)
        } else if (oldItem is ActivitiesListItem.WidgetCollectionHolder
            && newItem is ActivitiesListItem.WidgetCollectionHolder
        ) {
            oldItem.widgets == newItem.widgets
        } else {
            true
        }
    }

    override fun areItemsTheSame(
        oldItem: ActivitiesListItem,
        newItem: ActivitiesListItem,
    ): Boolean {
        return if (oldItem is ActivitiesListItem.Card
            && newItem is ActivitiesListItem.Card
        ) {
            oldItem.itemId == newItem.itemId
        } else if (oldItem is ActivitiesListItem.WidgetCollectionHolder && newItem is ActivitiesListItem.WidgetCollectionHolder) {
            oldItem.key == newItem.key
        } else {
            true
        }
    }

    private fun isFavoriteUpdate(
        oldItem: ActivitiesListItem.Card,
        newItem: ActivitiesListItem.Card,
    ) = oldItem.isFavorite != newItem.isFavorite
}

internal sealed interface ActivitiesListItem : KiboSerializable<ActivitiesListItem> {
    data class WidgetCollectionHolder(
        val key: Int,
        val widgets: List<WidgetCollectionView.WidgetItem>,
    ) : ActivitiesListItem {
        override fun getSerializer(): KSerializer<ActivitiesListItem> = serializer()
    }

    data class Card(
        override val itemId: Long,
        val name: String,
        val subtitle: String?,
        val photo: String?,
        val order: Int?,
        val isFavorite: Boolean?,
    ) : ActivitiesListItem, Favoriteable<Long> {
        override fun getSerializer(): KSerializer<ActivitiesListItem> = serializer()
    }
}

internal fun ContentActivity.toListItem(
    localizationService: LocalizationService,
    favoriteIds: Set<Long>,
): ActivitiesListItem.Card {
    val localizedName = localizationService.getString(name)
    val localizedSubtitle = subtitle?.let { localizationService.getString(it) }
    return ActivitiesListItem.Card(
        itemId,
        localizedName,
        localizedSubtitle,
        photos.firstOrNull(),
        order,
        favoriteIds.contains(itemId)
    )
}
