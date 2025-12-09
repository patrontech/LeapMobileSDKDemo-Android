package com.greencopper.event.scheduleItem

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.scheduleItem.ui.schedule.ScheduleListFragment
import com.greencopper.event.scheduleItem.viewmodel.DisplayMode
import com.greencopper.event.scheduleItem.viewmodel.Search
import com.greencopper.event.scheduleItem.viewmodel.SelectedView
import com.greencopper.event.scheduleItem.viewmodel.TimelineData
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.toLayoutData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class ScheduleInitializer : ParameterizedFeatureInitializer<ScheduleData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Event.Schedule", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): ScheduleData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: ScheduleData): Layout = ScheduleListFragment(
        ScheduleLayoutData(
            title = params.title,
            displayImages = params.displayImages,
            emptyScheduleImage = params.emptyScheduleImage,
            onScheduleItemTap = params.onScheduleItemTap,
            defaultUI = params.defaultUI,
            filtering = params.filtering,
            reminders = params.reminders,
            timeline = params.timeline,
            search = params.search,
            widgetCollections = params.collections?.map { it.toLayoutData() },
            favoritesEditing = params.favoritesEditing,
            myFavorites = params.myFavorites,
            myInterests = params.myInterests,
            hideEndTime = params.hideEndTime ?: false,
            displayMode = params.mode ?: DisplayMode.DAILY,
            editMyInterests = params.editMyInterests,
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: ScheduleData): RedirectionHash =
        RedirectionHash(key, params.analytics.screenName)
}

@Serializable
internal data class ScheduleData(
    val title: String? = null,
    val displayImages: Boolean = true,
    val emptyScheduleImage: String,
    val onScheduleItemTap: String,
    val defaultUI: SelectedView,
    val filtering: FilteringInfo? = null,
    val reminders: Reminders? = null,
    val search: Search? = null,
    val timeline: TimelineData? = null,
    val collections: List<WidgetCollectionData>? = null,
    val favoritesEditing: FavoritesEditing? = null,
    val myFavorites: FavoriteConfig? = null,
    val myInterests: IntegratedInterestsData? = null,
    val hideEndTime: Boolean? = null,
    val mode: DisplayMode? = null,
    val editMyInterests: EditMyInterests? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<ScheduleData> {

    override fun getSerializer(): KSerializer<ScheduleData> = serializer()

    @Serializable
    data class Reminders(val onTap: String)

    @Serializable
    data class EditMyInterests(val onTap: String)
}

@Serializable
internal data class ScheduleLayoutData(
    val title: String?,
    val displayImages: Boolean,
    val emptyScheduleImage: String,
    val onScheduleItemTap: String,
    val filtering: FilteringInfo?,
    val reminders: ScheduleData.Reminders?,
    val defaultUI: SelectedView,
    val timeline: TimelineData? = null,
    val search: Search?,
    val widgetCollections: List<WidgetCollectionCellLayoutData>? = null,
    val favoritesEditing: FavoritesEditing?,
    val myFavorites: FavoriteConfig?,
    val myInterests: IntegratedInterestsData? = null,
    val hideEndTime: Boolean = false,
    val displayMode: DisplayMode,
    val editMyInterests: ScheduleData.EditMyInterests? = null,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<ScheduleLayoutData> {
    override fun getSerializer(): KSerializer<ScheduleLayoutData> = serializer()
}
