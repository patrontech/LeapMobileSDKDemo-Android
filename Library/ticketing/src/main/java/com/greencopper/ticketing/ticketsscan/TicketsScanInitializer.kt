package com.greencopper.ticketing.ticketsscan

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.ticketing.providers.ProviderInfo
import com.greencopper.ticketing.providers.ProviderResolver
import com.greencopper.ticketing.ticketsscan.ui.TicketsScanFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class TicketsScanInitializer(
    private val providerResolver: ProviderResolver
) : ParameterizedFeatureInitializer<TicketsScanData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Ticketing.TicketsScan", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): TicketsScanData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: TicketsScanData): Layout {
        providerResolver.resolve(params.provider) //Check that showclixApi is available

        return TicketsScanFragment(
            TicketsScanLayoutData(
                provider = params.provider,
                featureLink = params.featureLink,
                analytics = params.analytics,
                redirectionHash = redirectionHashForParams(params)
            )
        )
    }

    override fun redirectionHashForParams(params: TicketsScanData): RedirectionHash =
        RedirectionHash(key, params.provider.key.name)
}

@Serializable
internal data class TicketsScanData(
    val provider: ProviderInfo,
    val featureLink: String,
    val analytics: ScreenNameAnalytics
) : KiboSerializable<TicketsScanData> {

    override fun getSerializer(): KSerializer<TicketsScanData> = serializer()
}

@Serializable
internal data class TicketsScanLayoutData(
    val provider: ProviderInfo,
    val featureLink: String,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash
) : KiboSerializable<TicketsScanLayoutData> {
    override fun getSerializer(): KSerializer<TicketsScanLayoutData> = serializer()
}
