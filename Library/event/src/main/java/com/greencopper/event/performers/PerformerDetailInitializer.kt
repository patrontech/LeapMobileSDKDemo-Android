package com.greencopper.event.performers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.performers.ui.performerdetail.PerformerDetailFragment
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

internal class PerformerDetailInitializer : ParameterizedFeatureInitializer<PerformerDetailData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Event.PerformerDetail", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): PerformerDetailData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: PerformerDetailData): Layout = PerformerDetailFragment(
        PerformerDetailLayoutData(
            performerId = params.performerId,
            stageDetailIcon = params.stageDetailIcon,
            myScheduleEditingInfo = params.myScheduleEditing,
            displayableTags = params.displayableTags,
            onScheduleItemTap = params.onScheduleItemTap,
            favoritesEditing = params.favoritesEditing,
            hideEndTime = params.hideEndTime ?: false,
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params),
        )
    )

    override fun redirectionHashForParams(params: PerformerDetailData): RedirectionHash =
        RedirectionHash(key, params.performerId)
}

@Serializable
internal data class PerformerDetailData(
    val performerId: String,
    val stageDetailIcon: String,
    val myScheduleEditing: MyScheduleEditingInfo? = null,
    val displayableTags: List<DisplayableTag> = emptyList(),
    val onScheduleItemTap: String? = null,
    val favoritesEditing: FavoritesEditing? = null,
    val hideEndTime: Boolean? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<PerformerDetailData> {

    override fun getSerializer(): KSerializer<PerformerDetailData> = serializer()
}

@Serializable
internal data class PerformerDetailLayoutData(
    val performerId: String,
    val stageDetailIcon: String,
    val myScheduleEditingInfo: MyScheduleEditingInfo?,
    val displayableTags: List<DisplayableTag>,
    val onScheduleItemTap: String?,
    val favoritesEditing: FavoritesEditing?,
    val hideEndTime: Boolean = false,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<PerformerDetailLayoutData> {
    override fun getSerializer(): KSerializer<PerformerDetailLayoutData> = serializer()
}
