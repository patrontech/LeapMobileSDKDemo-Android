package com.greencopper.interfacekit.rootview

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.tabBar.TabBarData
import com.greencopper.interfacekit.tabBar.TabBarInitializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class RootViewConfiguration(val feature: FeatureInfo): KiboSerializable<RootViewConfiguration> {
    override fun getSerializer(): KSerializer<RootViewConfiguration> = serializer()

    internal fun removeTabBarData(): RootViewConfiguration {
        return if (feature.key == TabBarInitializer.key && feature.params != null) {
            val tabBarData = KiboSerializable.decodeFromJsonElement<TabBarData>(feature.params)
            val firstTab = tabBarData.items.firstOrNull { it.display is TabBarData.Display.Embedded} ?: return this
            val firstFeature = firstTab.display as TabBarData.Display.Embedded
            RootViewConfiguration(firstFeature.feature)
        } else {
            this
        }
    }
}
