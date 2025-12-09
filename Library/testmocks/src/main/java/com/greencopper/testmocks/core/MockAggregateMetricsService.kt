package com.greencopper.testmocks.core

import android.content.Context
import com.greencopper.core.metrics.Metrics
import com.greencopper.core.metrics.service.AggregateMetricsService
import kotlin.reflect.KClass

public class MockAggregateMetricsService : AggregateMetricsService {
    public var onActivityStartCalled: Boolean = false
    override fun onActivityStart(activityContext: Context) {
        onActivityStartCalled = true
    }

    public var onActivityStopCalled: Boolean = false
    override fun onActivityStop(activityContext: Context) {
        onActivityStopCalled = true
    }

    public val trackedMetrics: MutableList<Metrics> = mutableListOf<Metrics>()
    override fun track(metrics: Metrics) {
        trackedMetrics.add(metrics)
    }

    public fun wasMetricTracked(metrics: KClass<*>): Boolean =
        trackedMetrics.any { it::class == metrics }
}
