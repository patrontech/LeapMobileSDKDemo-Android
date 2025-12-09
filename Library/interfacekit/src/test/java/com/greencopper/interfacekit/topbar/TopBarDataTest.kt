package com.greencopper.interfacekit.topbar

import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Test

internal class TopBarDataTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun topBarData_serializeDeserialize() {
        val data = TopBarData(
            title = "title",
            rightButtons = listOf(
                TopBarButton.ImageButton(
                    imageName = "imageName",
                    accessibilityLabel = "label",
                    shouldColor = false,
                    onTap = TopBarButton.OnTap("route", ItemNameAnalytics("name"))
                )
            ),
            leftButtons = listOf(
                TopBarButton.TextButton(
                    text = "text",
                    onTap = TopBarButton.OnTap("route", ItemNameAnalytics("name"))
                )
            )
        )

        testKiboSerializable(data)
    }
}
