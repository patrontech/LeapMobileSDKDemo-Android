package com.greencopper.event.activity.ui.activitylist

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.event.activity.ActivitiesListLayoutData
import com.greencopper.event.activity.viewmodel.ActivitiesListViewModel
import com.greencopper.event.colors.EventColor
import com.greencopper.event.metrics.activitiesList
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.color.DefaultColors
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

internal class ActivitiesListFragment : ListFragment<ActivitiesListLayoutData> {

    constructor(activitiesListData: ActivitiesListLayoutData) : super(activitiesListData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override val screenColor: ScreenColor
        get() = EventColor.activitiesList

    override val backgroundColor: Int
        get() = EventColor.activitiesList.background

    override val filteringBarColors: FilteringBarColor
        get() = EventColor.activitiesList.filters

    override val filteringBarTextStyle: FilteringBarTextStyle
        get() = EventTextStyle.activitiesList.filters

    override val emptyViewColors: EmptyViewColors
        get() = EventColor.activitiesList.empty

    override val emptyViewTextStyle: EmptyViewTextStyle
        get() = EventTextStyle.activitiesList.empty

    override val screenAnalytics: ScreenViewEvent
        get() = ScreenViewEvent(Screen.activitiesList(data.analytics.screenName))

    override val defaultTopBarTitle: String = localizationService.getString("event.activities.title")
    override val topBarColor: TopBarColor get() = EventColor.activitiesList.topBar
    override val topBarTextStyle: TopBarTextStyle get() = EventTextStyle.activitiesList.topBar

    override lateinit var listAdapter: ListAdapter<*, JobAwareViewHolder>

    override val viewModel: ActivitiesListViewModel by viewModel {
        listOf(savedFiltering?.mode ?: Mode.DEFAULT, computeFilteringModes())
    }

    override val decoratorInfo: SimpleLineDecorator by lazy {
        SimpleLineDecorator(
            tintColor = EventColor.activitiesList.separator,
            showLast = false,
            drawableHorizontalPaddingDp = 24
        )
    }

    override fun setupAdapter() {
        listAdapter = ActivitiesListAdapter(
            this,
            data.analytics.screenName,
            data.displayImages,
            data.favoritesEditing?.translate(localizationService)?.toFavoriteIcons(),
            decoratorInfo,
            viewModel.myActivitiesManager,
        ) { activityItem ->
            openDetailPage(activityItem)
        }
        listAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    private fun openDetailPage(activityItem: ActivitiesListItem.Card) {
        routeController.resolveRouteLink(
            data.onActivityTap,
            this,
            mapOf("activityId" to activityItem.itemId.toString()),
        )
    }

    override fun restoreData(encodedData: String): ActivitiesListLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
