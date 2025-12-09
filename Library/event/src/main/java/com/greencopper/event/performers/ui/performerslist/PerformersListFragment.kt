package com.greencopper.event.performers.ui.performerslist

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.core.data.*
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.event.colors.EventColor
import com.greencopper.event.metrics.performersList
import com.greencopper.event.performers.PerformersListLayoutData
import com.greencopper.event.performers.viewmodel.PerformersListViewModel
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.color.TopBarColor
import com.greencopper.interfacekit.favorites.toFavoriteIcons
import com.greencopper.interfacekit.favorites.translate
import com.greencopper.interfacekit.filtering.FilteringHandler.*
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarColor
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarTextStyle
import com.greencopper.interfacekit.lists.ui.*
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.textstyle.subsystem.TopBarTextStyle
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel

internal class PerformersListFragment : ListFragment<PerformersListLayoutData> {
    constructor(locationListData: PerformersListLayoutData) : super(locationListData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override val screenColor: ScreenColor
        get() = EventColor.performersList

    override val backgroundColor: Int
        get() = EventColor.performersList.background

    override val filteringBarColors: FilteringBarColor
        get() = EventColor.performersList.filters

    override val filteringBarTextStyle: FilteringBarTextStyle
        get() = EventTextStyle.performersList.filters

    override val emptyViewColors: EmptyViewColors
        get() = EventColor.performersList.empty

    override val emptyViewTextStyle: EmptyViewTextStyle
        get() = EventTextStyle.performersList.empty

    override val screenAnalytics: ScreenViewEvent
        get() = ScreenViewEvent(Screen.performersList(data.analytics.screenName))

    override val topBarColor: TopBarColor get() = EventColor.performersList.topBar
    override val topBarTextStyle: TopBarTextStyle get() = EventTextStyle.performersList.topBar
    override val defaultTopBarTitle: String = localizationService.getString("event.performers.title")

    override lateinit var listAdapter: ListAdapter<*, JobAwareViewHolder>
    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.baseListToolbar,
            topBarColor,
            topBarTextStyle,
            localizationService.getString(data.topBar.title ?: defaultTopBarTitle),
        )

    override val viewModel: PerformersListViewModel by viewModel {
        listOf(savedFiltering?.mode ?: Mode.DEFAULT, computeFilteringModes())
    }

    override val decoratorInfo: SimpleLineDecorator by lazy {
        SimpleLineDecorator(
            tintColor = EventColor.performersList.separator,
            showLast = false,
            drawableHorizontalPaddingDp = 24
        )
    }

    override fun setupAdapter() {
        listAdapter = PerformersListAdapter(
            this,
            data.analytics.screenName,
            data.displayImages,
            data.favoritesEditing?.translate(localizationService)?.toFavoriteIcons(),
            decoratorInfo,
            viewModel.myPerformersManager
        ) { performerItem ->
            openDetailPage(performerItem)
        }
        listAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    private fun openDetailPage(performerItem: PerformersListItem.Card) {
        routeController.resolveRouteLink(
            data.onPerformerTap,
            this,
            mapOf("performerId" to "\"${performerItem.itemId}\""),
        )
    }

    override fun restoreData(encodedData: String): PerformersListLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}



