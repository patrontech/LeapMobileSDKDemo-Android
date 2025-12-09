package com.greencopper.interfacekit.tabBar

import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Test

internal class TabBarDataTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun kiboserializable() {
        val items = mutableListOf<TabBarData.Item>()
        val embeddedDisplay = TabBarData.Display.Embedded(
            feature = FeatureInfo(
                FeatureKey(
                    "Schedule.List",
                    1
                ),
                null
            )
        )
        val embeddedScheduleItem = TabBarData.Item(
            name = "Schedule",
            iconName = "placeholder.png",
            display = embeddedDisplay,
            analytics = ItemNameAnalytics("TestItem")
        )
        items.add(embeddedScheduleItem)

        val routingDisplay = TabBarData.Display.Routing(
            Route.Present(
                feature = FeatureInfo(
                    FeatureKey(
                        "Schedule.List2",
                        1
                    ),
                    null
                )
            )
        )
        val presentScheduleItem = TabBarData.Item(
            name = "Schedule2",
            iconName = "placeholder2.png",
            display = routingDisplay,
            analytics = ItemNameAnalytics("TestItem")
        )
        items.add(presentScheduleItem)

        val tabBarParameters = TabBarData(
            selectedIndex = 0,
            items = items
        )

        testKiboSerializable(tabBarParameters)
    }
}
