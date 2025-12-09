package com.greencopper.maps.locationlist.ui

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.color.TopBarColor
import com.greencopper.interfacekit.favorites.toFavoriteIcons
import com.greencopper.interfacekit.favorites.translate
import com.greencopper.interfacekit.filtering.FilteringHandler.Mode
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarColor
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarTextStyle
import com.greencopper.interfacekit.lists.ui.*
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.textstyle.subsystem.TopBarTextStyle
import com.greencopper.interfacekit.ui.SimpleLineDecorator
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.viewModel
import com.greencopper.maps.colors.MapsColor
import com.greencopper.maps.locationlist.LocationListLayoutData
import com.greencopper.maps.locationlist.LocationListViewModel
import com.greencopper.maps.metrics.locationList
import com.greencopper.maps.textstyle.MapsTextStyle

internal class LocationListFragment : ListFragment<LocationListLayoutData> {

    constructor(locationListData: LocationListLayoutData) : super(locationListData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override val screenColor: ScreenColor
        get() = MapsColor.locationsList

    override val backgroundColor: Int
        get() = MapsColor.locationsList.background

    override val filteringBarColors: FilteringBarColor
        get() = MapsColor.locationsList.filters

    override val filteringBarTextStyle: FilteringBarTextStyle
        get() = MapsTextStyle.locationList.filters

    override val emptyViewColors: EmptyViewColors
        get() = MapsColor.locationsList.empty

    override val emptyViewTextStyle: EmptyViewTextStyle
        get() = MapsTextStyle.locationList.empty

    override val screenAnalytics: ScreenViewEvent
        get() = ScreenViewEvent(Screen.locationList(data.analytics.screenName))

    override val topBarColor: TopBarColor get() = MapsColor.locationsList.topBar
    override val topBarTextStyle: TopBarTextStyle get() = MapsTextStyle.locationList.topBar
    override val defaultTopBarTitle: String = localizationService.getString("maps.locations_list.title")

    override lateinit var listAdapter: ListAdapter<*, JobAwareViewHolder>

    override val decoratorInfo: SimpleLineDecorator by lazy {
        SimpleLineDecorator(
            tintColor = MapsColor.locationsList.separator,
            showLast = false,
            drawableHorizontalPaddingDp = 24
        )
    }

    override val viewModel: LocationListViewModel by viewModel {
        listOf(savedFiltering?.mode ?: Mode.DEFAULT, computeFilteringModes())
    }

    override fun setupAdapter() {
        listAdapter = LocationListAdapter(
            data.displayImages,
            ::onLocationTap,
            this,
            data.favoritesEditing?.translate(localizationService)?.toFavoriteIcons(),
            data.analytics.screenName,
            decoratorInfo,
            viewModel.myLocationsManager,
        )
        listAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    private fun onLocationTap(locationItem: LocationListItem.LocationItem) {
        routeController.resolveRouteLink(
            data.onLocationTap,
            this,
            mapOf("locationId" to "\"${locationItem.itemId}\"")
        )
    }

    override fun restoreData(encodedData: String): LocationListLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
