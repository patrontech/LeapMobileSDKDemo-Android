package com.greencopper.event.performers.ui.performerslist

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
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.PerformerItemBinding
import com.greencopper.event.performers.Performer
import com.greencopper.event.performers.ui.AddToMyPerformersAnalytics
import com.greencopper.event.performers.ui.RemoveFromMyPerformersAnalytics
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

internal class PerformersListAdapter(
    private val origin: Layout,
    private val screenName: String,
    private val displayImages: Boolean,
    private val favoriteIcons: FavoriteIcons? = null,
    private val widgetCollectionCellSeparator: BottomDrawableItemDecorator.DecoratorInfos,
    private val myPerformersManager: FavoritesManager<String>,
    private val onPerformerItemClicked: (PerformersListItem.Card) -> Unit,
) : ListAdapter<PerformersListItem, JobAwareViewHolder>(PerformersListAdapterDiffUtil()) {

    private val inflater: LayoutInflater = origin.layoutInflater

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PerformersListItem.Card -> VIEW_TYPE_CARD
            is PerformersListItem.WidgetCollectionHolder -> WidgetCollectionCell.ADAPTER_TYPE
        }
    }

    internal inner class PerformersViewHolder(
        private val binding: PerformerItemBinding,
    ) : JobAwareViewHolder(binding.root) {

        private val cellColors = EventColor.performersList.cell

        internal fun setupColors() {
            val textStyles = EventTextStyle.performersList.cell

            with(binding) {
                performerItemTitle.setTextColor(cellColors.name)
                performerItemTitle.setFont(textStyles.name)
                performerItemSubtitle.setTextColor(cellColors.subtitle)
                performerItemSubtitle.setFont(textStyles.subtitle)
                performerItemContainer.background = ColorDrawable(Color.WHITE).apply {
                    setTintList(cellColors.background.toColorStateList())
                }
            }
        }

        internal fun bind(data: PerformersListItem.Card) {
            with(binding) {
                cardView.isVisible = displayImages
                performerItemPicture.resetImageFrom(
                    data.photo,
                    origin.viewLifecycleOwner.lifecycleScope,
                    format = THUMBNAIL,
                )?.also { jobs.add(it) }

                performerItemTitle.text = data.name
                performerItemSubtitle.setTextOrGone(data.subtitle)
                performerItemContainer.setOnSafeClickListener { onPerformerItemClicked(data) }
                favoriteIcons?.let {
                    bindFavoriteData(it, data)
                } ?: run {
                    performerItemFavoriteAddRemove.isVisible = false
                }
            }
        }

        private fun bindFavoriteData(icons: FavoriteIcons, data: PerformersListItem.Card) {
            with(binding.performerItemFavoriteAddRemove) {
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
                setColorFilter(cellColors.myPerformerIcon)
                setOnSafeClickListener { updateFavorite(data) }
            }
        }

        private fun updateFavorite(item: PerformersListItem.Card) {
            if (item.isFavorite == true) {
                myPerformersManager.removeFromFavorites(item)
                App.track(
                    RemoveFromMyPerformersAnalytics(
                        screenName = screenName,
                        itemId = item.itemId,
                        itemName = item.name
                    )
                )
            } else {
                myPerformersManager.addToFavorites(item)
                App.track(
                    AddToMyPerformersAnalytics(
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
                PerformersViewHolder(PerformerItemBinding.inflate(inflater, parent, false)).also {
                    it.setupColors()
                }
            }

            WidgetCollectionCell.ADAPTER_TYPE -> {
                val widgetsView = WidgetCollectionCellBinding.inflate(inflater, parent, false)
                WidgetCollectionCell(widgetsView)
            }

            else -> throw IllegalStateException("${PerformersListAdapter::class.simpleName} not set up properly")
        }
    }

    override fun onBindViewHolder(holder: JobAwareViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is PerformersViewHolder -> holder.bind(item as PerformersListItem.Card)
            is WidgetCollectionCell -> holder.bind(
                (item as PerformersListItem.WidgetCollectionHolder).widgets,
                origin,
                screenName,
                widgetCollectionCellSeparator.takeIf { currentList.lastIndex != position || it.showLast },
                position == 0,
                position == itemCount - 1
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

private class PerformersListAdapterDiffUtil : DiffUtil.ItemCallback<PerformersListItem>() {
    override fun areContentsTheSame(
        oldItem: PerformersListItem,
        newItem: PerformersListItem,
    ): Boolean {
        return if (oldItem is PerformersListItem.Card
            && newItem is PerformersListItem.Card
        ) {
            oldItem == newItem && !isFavoriteUpdate(oldItem, newItem)
        } else if (oldItem is PerformersListItem.WidgetCollectionHolder
            && newItem is PerformersListItem.WidgetCollectionHolder
        ) {
            oldItem.widgets == newItem.widgets
        } else {
            true
        }
    }

    override fun areItemsTheSame(
        oldItem: PerformersListItem,
        newItem: PerformersListItem,
    ): Boolean {
        return if (oldItem is PerformersListItem.Card
            && newItem is PerformersListItem.Card
        ) {
            oldItem.itemId == newItem.itemId
        } else if (oldItem is PerformersListItem.WidgetCollectionHolder && newItem is PerformersListItem.WidgetCollectionHolder) {
            oldItem.key == newItem.key
        } else {
            true
        }
    }

    private fun isFavoriteUpdate(
        oldItem: PerformersListItem.Card,
        newItem: PerformersListItem.Card,
    ) = oldItem.isFavorite != newItem.isFavorite
}

internal sealed interface PerformersListItem : KiboSerializable<PerformersListItem> {
    data class WidgetCollectionHolder(
        val key: Int,
        val widgets: List<WidgetCollectionView.WidgetItem>,
    ) : PerformersListItem {
        override fun getSerializer(): KSerializer<PerformersListItem> = serializer()
    }

    data class Card(
        override val itemId: String,
        val name: String,
        val subtitle: String?,
        val photo: String?,
        val order: Int?,
        val isFavorite: Boolean?,
    ) : PerformersListItem, Favoriteable<String> {
        override fun getSerializer(): KSerializer<PerformersListItem> = serializer()
    }
}

internal fun Performer.toListItem(
    localizationService: LocalizationService,
    favoriteIds: Set<String>,
): PerformersListItem.Card {
    val localizedName = localizationService.getString(name)
    val localizedSubtitle = subtitle?.let { localizationService.getString(it) }
    return PerformersListItem.Card(
        itemId,
        localizedName,
        localizedSubtitle,
        photos.firstOrNull(),
        order,
        favoriteIds.contains(itemId)
    )
}
