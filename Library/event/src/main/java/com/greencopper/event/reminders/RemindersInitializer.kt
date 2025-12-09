package com.greencopper.event.reminders

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.reminders.ui.RemindersFragment
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class RemindersInitializer : ParameterizedFeatureInitializer<RemindersData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Event.ScheduleReminders", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): RemindersData = KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: RemindersData): Layout = RemindersFragment(
        RemindersLayoutData(
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: RemindersData): RedirectionHash =
        RedirectionHash(featureKey, params.analytics.screenName)
}

@Serializable
internal data class RemindersData(
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<RemindersData> {

    override fun getSerializer(): KSerializer<RemindersData> = serializer()
}

@Serializable
internal data class RemindersLayoutData(
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<RemindersLayoutData> {
    override fun getSerializer(): KSerializer<RemindersLayoutData> = serializer()
}

