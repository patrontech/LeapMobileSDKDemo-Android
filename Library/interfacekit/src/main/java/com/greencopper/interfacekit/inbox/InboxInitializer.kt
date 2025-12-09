package com.greencopper.interfacekit.inbox

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.inbox.ui.InboxFragment
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.topbar.TopBarData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class InboxInitializer :
    ParameterizedFeatureInitializer<InboxData>() {

    companion object {
        val key = FeatureKey("InterfaceKit.Inbox", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): InboxData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: InboxData): Layout = InboxFragment(
        InboxLayoutData(
            topBar = params.topBar,
            inboxApiUrl = params.inboxApiUrl,
            emptyStateImage = params.emptyStateImage,
            timezone = params.timezone,
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: InboxData): RedirectionHash =
        RedirectionHash(featureKey, params.analytics.screenName)
}

@Serializable
internal data class InboxData(
    val topBar: TopBarData? = null,
    val inboxApiUrl: String,
    val emptyStateImage: String,
    val timezone: String? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<InboxData> {

    override fun getSerializer(): KSerializer<InboxData> = serializer()
}

@Serializable
internal data class InboxLayoutData(
    val topBar: TopBarData? = null,
    val inboxApiUrl: String,
    val emptyStateImage: String,
    val timezone: String?,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<InboxLayoutData> {

    override fun getSerializer(): KSerializer<InboxLayoutData> = serializer()
}
