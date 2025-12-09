package com.greencopper.event.activity

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.activity.ui.activitydetail.ActivityDetailFragment
import com.greencopper.event.scheduleItem.data.MyScheduleEditingInfo
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.tags.DisplayableTag
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class ActivityDetailInitializer: ParameterizedFeatureInitializer<ActivityDetailData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Event.ActivityDetail", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): ActivityDetailData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: ActivityDetailData): Layout = ActivityDetailFragment(
        ActivityDetailLayoutData(
            activityId = params.activityId,
            myScheduleEditingInfo = params.myScheduleEditing,
            stageDetailIcon = params.stageDetailIcon,
            favoritesEditing = params.favoritesEditing,
            displayableTags = params.displayableTags,
            onScheduleItemTap = params.onScheduleItemTap,
            hideEndTime = params.hideEndTime ?: false,
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params),
        )
    )

    override fun redirectionHashForParams(params: ActivityDetailData): RedirectionHash =
        RedirectionHash(key, params.activityId.toString())
}

@Serializable
internal data class ActivityDetailData(
    val activityId: Long,
    val myScheduleEditing: MyScheduleEditingInfo? = null,
    val stageDetailIcon: String,
    val favoritesEditing: FavoritesEditing? = null,
    val displayableTags: List<DisplayableTag> = emptyList(),
    val onScheduleItemTap: String? = null,
    val hideEndTime: Boolean? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<ActivityDetailData> {

    override fun getSerializer(): KSerializer<ActivityDetailData> = serializer()
}

@Serializable
internal data class ActivityDetailLayoutData(
    val activityId: Long,
    val myScheduleEditingInfo: MyScheduleEditingInfo?,
    val stageDetailIcon: String,
    val favoritesEditing: FavoritesEditing?,
    val displayableTags: List<DisplayableTag>,
    val onScheduleItemTap: String?,
    val hideEndTime: Boolean = false,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<ActivityDetailLayoutData> {
    override fun getSerializer(): KSerializer<ActivityDetailLayoutData> = serializer()
}
