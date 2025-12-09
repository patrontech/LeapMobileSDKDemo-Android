package com.greencopper.interfacekit.widgets.analytics

import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.testmocks.core.MockingMappedProvider
import com.greencopper.testmocks.shouldBe
import org.junit.jupiter.api.Test

internal class WidgetAnalyticsTest {

    @Test
    fun `buildWidgetAnalytics with all params should return filled Map`() {
        val widgetCategory = "widgetCategory"
        val widgetName = "widgetName"
        val screenName = "screenName"
        val widgetId = "id"

        val result = buildWidgetAnalytics(widgetCategory, widgetName, screenName, widgetId)

        result.size shouldBe 4
        result[EventParameter.itemCategory] shouldBe widgetCategory
        result[EventParameter.screenName] shouldBe screenName
        result[EventParameter.itemName] shouldBe widgetName
        result[EventParameter.itemId] shouldBe widgetId
    }

    @Test
    fun `buildWidgetAnalytics with partial params should return partial Map`() {
        val widgetCategory = "widgetCategory"
        val widgetName = null
        val screenName = "screenName"
        val widgetId = null

        val result = buildWidgetAnalytics(widgetCategory, widgetName, screenName, widgetId)

        result.size shouldBe 2
        result[EventParameter.itemCategory] shouldBe widgetCategory
        result[EventParameter.screenName] shouldBe screenName
    }

    @Test
    fun `WidgetEventAnalytics tracking should work`() {
        val provider = MockingMappedProvider().apply {
            enable()
        }
        val eventName = EventName("widget")
        val analytics = mapOf(EventParameter("screenName") to "screen")

        val event = WidgetEventAnalytics(eventName, analytics)

        event.track(provider)

        assert(provider.wasMetricTracked(eventName, analytics))
    }
}

