package com.greencopper.core.metrics

import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.provider.MappedProvider

internal fun Screen.Companion.testingScreen(name: String): Screen = Screen(name, "testingScreen")
internal fun EventName.Companion.testEventName(): EventName = EventName("testName")
internal fun EventParameter.Companion.testParameterName(): EventParameter =
    EventParameter("testParameterName")

internal data class TestScreenViewEvent(val screen: Screen) : Metrics, MappedMetrics {

    override fun track(provider: MappedProvider) {
        provider.track(
            EventName.testEventName(),
            mutableMapOf(EventParameter.testParameterName() to screen.name)
        )
    }
}