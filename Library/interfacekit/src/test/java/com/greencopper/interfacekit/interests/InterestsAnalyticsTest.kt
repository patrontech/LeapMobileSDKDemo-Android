package com.greencopper.interfacekit.interests

import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.itemId
import com.greencopper.core.metrics.labels.itemName
import com.greencopper.interfacekit.interests.recipe.Interest
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.core.MockingMappedProvider
import com.greencopper.testmocks.shouldBe
import org.junit.jupiter.api.Test

internal class InterestsAnalyticsTest {

    private val mappingProvider = MockingMappedProvider().apply {
        enable()
    }

    @Test
    fun interestSelected_shouldTrack() {
        val analytic = InterestSelected("name1", "1", "screenName1")

        analytic.track(mappingProvider)

        mappingProvider.wasMetricTracked(
            event = EventName("interests_picker/select"),
            parameters = mapOf(
                EventParameter.itemName to "name1",
                EventParameter.itemId to "1",
                EventParameter.screenName to "screenName1",
            )
        ) shouldBe true
    }

    @Test
    fun interestUnselected_shouldTrack() {
        val analytic = InterestUnselected("name1", "1", "screenName1")

        analytic.track(mappingProvider)

        mappingProvider.wasMetricTracked(
            event = EventName("interests_picker/unselect"),
            parameters = mapOf(
                EventParameter.itemName to "name1",
                EventParameter.itemId to "1",
                EventParameter.screenName to "screenName1",
            )
        ) shouldBe true
    }

    @Test
    fun interestPickerClosed_shouldTrack() {
        val localizationService = MockLocalizationService()
        val interests = listOf(
            Interest("1", "name1", 1, "analyticsName1"),
            Interest("2", "name2", 1, "analyticsName2"),
        )
        val analytic = InterestsPickerClosed(localizationService, interests, "screenName1")

        analytic.track(mappingProvider)

        mappingProvider.wasMetricTracked(
            event = EventName("interests_picker/close"),
            parameters = mapOf(
                EventParameter("selectedItems") to "analyticsName1(1), analyticsName2(2)",
                EventParameter.screenName to "screenName1",
            )
        ) shouldBe true
    }
}
