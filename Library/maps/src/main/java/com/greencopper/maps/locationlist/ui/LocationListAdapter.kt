package com.greencopper.maps.locationlist.ui

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
import com.greencopper.core.services.track
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.WidgetCollectionCellBinding
import com.greencopper.interfacekit.favorites.*
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCell
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.kiba_maps.databinding.LocationItemBinding
import com.greencopper.maps.colors.MapsColor
import com.greencopper.maps.common.ui.AddToMyLocationsAnalytics
import com.greencopper.maps.common.ui.RemoveFromMyLocationsAnalytics
import com.greencopper.maps.textstyle.MapsTextStyle
import com.greencopper.toolkit.App
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

internal class LocationListAdapter(
    private val displayImages: Boolean,
    private val onLocationItemClicked: (LocationListItem.LocationItem) -> Unit,
    private val origin: Layout,
    private val favoriteIcons: FavoriteIcons? = null,
    private val screenName: String,
    private val widgetCollectionCellSeparator: BottomDrawableItemDecorator.DecoratorInfos,
    private val myLocationsManager: FavoritesManager<String>,
) : ListAdapter<LocationListItem, JobAwareViewHolder>(LocationListAdapterDiffUtil()) {

    inner class LocationViewHolder(
        private val locationBinding: LocationItemBinding,
    ) : JobAwareViewHolder(locationBinding.root) {
        private val cellColors = MapsColor.locationsList.cell

        internal fun setupColors() {
            with(locationBinding) {
                val cellTextStyle = MapsTextStyle.locationList.cell
                cardView.isVisible = displayImages
                locationItemTitle.setTextColor(cellColors.name)
                locationItemTitle.setFont(cellTextStyle.name)
                locationItemSubtitle.setTextColor(cellColors.subtitle)
                locationItemSubtitle.setFont(cellTextStyle.subtitle)

                val backgroundDrawable = ColorDrawable(Color.WHITE)
                backgroundDrawable.setTintList(cellColors.background.toColorStateList())
                locationItemBackground.background = backgroundDrawable
            }
        }

        internal fun bind(locationItem: LocationListItem.LocationItem) {
            with(locationBinding) {
                locationItemImageView.resetImageFrom(
                    locationItem.photo,
                    origin.viewLifecycleOwner.lifecycleScope,
                    format = THUMBNAIL,
                )?.also { jobs.add(it) }
                locationItemTitle.text = locationItem.name
                locationItemSubtitle.setTextOrGone(locationItem.subtitle)

                favoriteIcons?.let {
                    bindFavoriteData(favoriteIcons, locationItem)
                } ?: run {
                    locationItemFavoriteAddRemove.isVisible = false
                }

                locationItemBackground.setOnSafeClickListener { onLocationItemClicked(locationItem) }
            }
        }

        private fun bindFavoriteData(icons: FavoriteIcons, data: LocationListItem.LocationItem) {
            with(locationBinding.locationItemFavoriteAddRemove) {
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
                setColorFilter(cellColors.myLocationIcon)
                setOnSafeClickListener { updateFavorite(data) }
            }
        }

        private fun updateFavorite(item: LocationListItem.LocationItem) {
            if (item.isFavorite == true) {
                myLocationsManager.removeFromFavorites(item)
                App.track(
                    RemoveFromMyLocationsAnalytics(
                        screenName = screenName,
                        itemId = item.itemId,
                        itemName = item.name
                    )
                )
            } else {
                myLocationsManager.addToFavorites(item)
                App.track(
                    AddToMyLocationsAnalytics(
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
            VIEW_TYPE_LOCATION_ITEM -> {
                val binding = LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                LocationViewHolder(binding).apply {
                    setupColors()
                }
            }

            WidgetCollectionCell.ADAPTER_TYPE -> {
                val widgetsView =
                    WidgetCollectionCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                WidgetCollectionCell(widgetsView)
            }

            else -> throw IllegalStateException("${LocationListAdapter::class.simpleName} not set up properly")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is LocationListItem.LocationItem -> VIEW_TYPE_LOCATION_ITEM
            is LocationListItem.WidgetCollectionHolder -> WidgetCollectionCell.ADAPTER_TYPE
        }
    }

    override fun onBindViewHolder(holder: JobAwareViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is LocationViewHolder -> holder.bind(item as LocationListItem.LocationItem)
            is WidgetCollectionCell -> holder.bind(
                (item as LocationListItem.WidgetCollectionHolder).widgets,
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
        private const val VIEW_TYPE_LOCATION_ITEM = 1
    }
}

private class LocationListAdapterDiffUtil : DiffUtil.ItemCallback<LocationListItem>() {

    override fun areContentsTheSame(
        oldItem: LocationListItem,
        newItem: LocationListItem,
    ): Boolean {
        return if (oldItem is LocationListItem.LocationItem
            && newItem is LocationListItem.LocationItem
        ) {
            oldItem == newItem && !isFavoriteUpdate(oldItem, newItem)
        } else if (oldItem is LocationListItem.WidgetCollectionHolder
            && newItem is LocationListItem.WidgetCollectionHolder
        ) {
            oldItem.widgets == newItem.widgets
        } else {
            true
        }
    }

    override fun areItemsTheSame(
        oldItem: LocationListItem,
        newItem: LocationListItem,
    ): Boolean {
        return if (oldItem is LocationListItem.LocationItem
            && newItem is LocationListItem.LocationItem
        ) {
            oldItem.itemId == newItem.itemId
        } else if (oldItem is LocationListItem.WidgetCollectionHolder && newItem is LocationListItem.WidgetCollectionHolder) {
            oldItem.key == newItem.key
        } else {
            true
        }
    }

    private fun isFavoriteUpdate(
        oldItem: LocationListItem.LocationItem,
        newItem: LocationListItem.LocationItem,
    ) = oldItem.isFavorite != newItem.isFavorite
}

internal sealed interface LocationListItem : KiboSerializable<LocationListItem> {
    data class LocationItem(
        override val itemId: String,
        val name: String,
        val subtitle: String?,
        val photo: String?,
        val isFavorite: Boolean?,
        val order: Int?,
    ) : LocationListItem, Favoriteable<String> {
        override fun getSerializer(): KSerializer<LocationListItem> = serializer()
    }

    data class WidgetCollectionHolder(
        val key: Int,
        val widgets: List<WidgetCollectionView.WidgetItem>,
    ) : LocationListItem {
        override fun getSerializer(): KSerializer<LocationListItem> = serializer()
    }
}
