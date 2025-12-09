package com.greencopper.core.metrics.service

import android.content.Context
import com.greencopper.core.metrics.*
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.provider.Provider
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockingLifecycleAwareProvider
import com.greencopper.testmocks.core.MockingMappedProvider
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcreteAggregateMetricsServiceTest: CoroutineTest(UnconfinedTestDispatcher()) {
    @Test
    fun whenTrackingMultipleProviders_multipleProvidersHaveTracking() {
        val mappingProvider1 = MockingMappedProvider(Provider("testProvider1"))
        mappingProvider1.enable()
        val mappingProvider2 = MockingMappedProvider(Provider("testProvider2"))
        mappingProvider2.enable()
        val aggregateMetricsService = ConcreteAggregateMetricsService(
            listOf(
                mappingProvider1,
                mappingProvider2
            ),
            testScope
        )
        val testScreenEvent = TestScreenViewEvent(Screen.testingScreen("MyScreen"))
        aggregateMetricsService.track(testScreenEvent)
        assertThat(
            mappingProvider1.wasMetricTracked(
                EventName.testEventName(),
                mutableMapOf(EventParameter.testParameterName() to testScreenEvent.screen.name)
            )
        ).isTrue
        assertThat(
            mappingProvider2.wasMetricTracked(
                EventName.testEventName(),
                mutableMapOf(EventParameter.testParameterName() to testScreenEvent.screen.name)
            )
        ).isTrue
    }

    @Test
    fun whenDisablingServices_notTracked() {
        val provider1 = Provider("testProvider1")
        val provider2 = Provider("testProvider2")
        val mappingProvider1 = MockingMappedProvider(provider1)
        mappingProvider1.enable()
        val mappingProvider2 = MockingMappedProvider(provider2)
        mappingProvider2.disable()
        val aggregateMetricsService = ConcreteAggregateMetricsService(
            listOf(
                mappingProvider1,
                mappingProvider2
            ),
            testScope
        )

        val testScreenEvent = TestScreenViewEvent(Screen.testingScreen("MyScreen"))
        aggregateMetricsService.track(testScreenEvent)
        assertThat(
            mappingProvider1.wasMetricTracked(
                EventName.testEventName(),
                mutableMapOf(EventParameter.testParameterName() to testScreenEvent.screen.name)
            )
        ).isTrue
        assertThat(
            mappingProvider2.wasMetricTracked(
                EventName.testEventName(),
                mutableMapOf(EventParameter.testParameterName() to testScreenEvent.screen.name)
            )
        ).isFalse
    }

    @Test
    fun whenAddingServices_allHaveTracking() {
        val provider1 = Provider("testProvider1")
        val provider2 = Provider("testProvider2")
        val mappingProvider1 = MockingMappedProvider(provider1)
        val mappingProvider2 = MockingMappedProvider(provider2)
        val aggregateMetricsService = ConcreteAggregateMetricsService(
            listOf(
                mappingProvider1,
                mappingProvider2
            ),
            testScope
        )
        mappingProvider1.enable()
        mappingProvider2.enable()

        val testScreenEvent = TestScreenViewEvent(Screen.testingScreen("MyScreen"))
        aggregateMetricsService.track(testScreenEvent)
        assertThat(
            mappingProvider1.wasMetricTracked(
                EventName.testEventName(),
                mutableMapOf(EventParameter.testParameterName() to testScreenEvent.screen.name)
            )
        ).isTrue
        assertThat(
            mappingProvider2.wasMetricTracked(
                EventName.testEventName(),
                mutableMapOf(EventParameter.testParameterName() to testScreenEvent.screen.name)
            )
        ).isTrue
    }

    @Test
    fun whenTrackingDifferentTypeOfMetrics() {
        val mappingProvider = MockingMappedProvider()
        val aggregateMetricsService = ConcreteAggregateMetricsService(
            listOf(mappingProvider),
            testScope
        )
        val metrics = object: Metrics{}
        aggregateMetricsService.track(metrics)
        assertThat(
            mappingProvider.wasMetricTracked(
                EventName.testEventName(),
                mutableMapOf()
            )
        ).isFalse
    }

    @Test
    fun checkCallingLifecycleAwareCallbacks() {
        val nonLifecycleAwareProvider = MockingMappedProvider()
        val lifecycleAwareProvider = MockingLifecycleAwareProvider()
        val aggregateMetricsService = ConcreteAggregateMetricsService(
            listOf(
                nonLifecycleAwareProvider,
                lifecycleAwareProvider
            ),
            testScope
        )

        val context = mockk<Context>(relaxed = true)

        aggregateMetricsService.onActivityStart(context)
        assertThat(lifecycleAwareProvider.activityStarted).isTrue

        aggregateMetricsService.onActivityStop(context)
        assertThat(lifecycleAwareProvider.activityStopped).isTrue
    }

    override fun afterEach() {}
}
