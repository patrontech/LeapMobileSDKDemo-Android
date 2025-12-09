package com.greencopper.event.scheduleItem.ui.scheduledetail

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.scheduleItem.data.MyScheduleEditingInfo
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.tags.DisplayableTag
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class ScheduleItemDetailInitializer: ParameterizedFeatureInitializer<ScheduleItemDetailData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Event.ScheduleItemDetail", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): ScheduleItemDetailData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: ScheduleItemDetailData): Layout = ScheduleItemDetailFragment(
        ScheduleItemDetailLayoutData(
            scheduleItemId = params.scheduleItemId,
            myScheduleEditingInfo = params.myScheduleEditing,
            displayableTags = params.displayableTags,
            stageDetailIcon = params.stageDetailIcon,
            hideEndTime = params.hideEndTime ?: false,
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: ScheduleItemDetailData): RedirectionHash =
        RedirectionHash(key, params.scheduleItemId.toString())
}

@Serializable
internal data class ScheduleItemDetailData(
    val scheduleItemId: Long,
    val myScheduleEditing: MyScheduleEditingInfo? = null,
    val displayableTags: List<DisplayableTag> = emptyList(),
    val stageDetailIcon: String,
    val hideEndTime: Boolean? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<ScheduleItemDetailData> {

    override fun getSerializer(): KSerializer<ScheduleItemDetailData> = serializer()
}

@Serializable
internal data class ScheduleItemDetailLayoutData(
    val scheduleItemId: Long,
    val myScheduleEditingInfo: MyScheduleEditingInfo?,
    val displayableTags: List<DisplayableTag>,
    val stageDetailIcon: String,
    val hideEndTime: Boolean = false,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<ScheduleItemDetailLayoutData> {
    override fun getSerializer(): KSerializer<ScheduleItemDetailLayoutData> = serializer()
}
