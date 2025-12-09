package com.greencopper.interfacekit.tabBar

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.tabBar.ui.TabBarFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class TabBarInitializer : ParameterizedFeatureInitializer<TabBarData>() {

    companion object {
        val key: FeatureKey = FeatureKey("InterfaceKit.TabBar", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): TabBarData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: TabBarData): Layout {
        val tabBarLayoutData = TabBarLayoutData(
            defaultTabIndex = params.selectedIndex,
            trackMetadata = params.trackMetadata,
            items = params.items,
            redirectionHash = redirectionHashForParams(params)
        )

        return TabBarFragment(tabBarLayoutData)
    }

    override fun redirectionHashForParams(params: TabBarData): RedirectionHash = RedirectionHash(key)
}

@Serializable
internal data class TabBarLayoutData(
    val defaultTabIndex: Int,
    val trackMetadata: Boolean?,
    val items: List<TabBarData.Item>,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<TabBarLayoutData> {
    override fun getSerializer(): KSerializer<TabBarLayoutData> = serializer()
}
