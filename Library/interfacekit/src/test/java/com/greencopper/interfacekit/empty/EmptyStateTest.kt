package com.greencopper.interfacekit.empty

import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.testmocks.shouldBe
import kotlinx.serialization.json.JsonArray
import org.junit.jupiter.api.Test

internal class EmptyStateTest {

    @Test
    fun testEquals() {
        val x = EmptyState(
            title = "honestatis",
            subtitle = "libris",
            imageName = "Derick Joyce",
            topWidgetCollection = WidgetCollectionConfiguration.Instance(
                widgets = listOf(
                    WidgetCollectionConfiguration.Instance.WidgetInfo(
                        WidgetCollectionConfiguration.Instance.WidgetKey(name = "testKey", version = 1),
                        JsonArray(emptyList()),
                    )
                ),
            ),
            "screenName"
        )
        val y = EmptyState(
            title = "honestatis",
            subtitle = "libris",
            imageName = "Derick Joyce",
            topWidgetCollection = WidgetCollectionConfiguration.Instance(
                widgets = listOf(
                    WidgetCollectionConfiguration.Instance.WidgetInfo(
                        WidgetCollectionConfiguration.Instance.WidgetKey(name = "testKey", version = 1),
                        JsonArray(emptyList()),
                    )
                ),
            ),
            "screenName"
        )

        x shouldBe y
        y shouldBe x
        x.hashCode() shouldBe y.hashCode()
    }
}
