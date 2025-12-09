package com.greencopper.core.metrics.service

import android.content.Context
import com.greencopper.core.metrics.Metrics
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.provider.LifecycleAwareProvider
import com.greencopper.core.metrics.provider.MappedProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public interface AggregateMetricsService : MetricsService {
    public fun onActivityStart(activityContext: Context)
    public fun onActivityStop(activityContext: Context)
}

internal class ConcreteAggregateMetricsService(
    private val providers: List<MappedProvider>,
    private val coroutineScope: CoroutineScope,
) : AggregateMetricsService {

    override fun track(metrics: Metrics) {
        coroutineScope.launch {
            providers.forEach {
                if (metrics is MappedMetrics) {
                    metrics.track(provider = it)
                }
            }
        }
    }

    override fun onActivityStart(activityContext: Context) {
        providers
            .filterIsInstance(LifecycleAwareProvider::class.java)
            .forEach {
                it.onActivityStart(activityContext)
            }
    }

    override fun onActivityStop(activityContext: Context) {
        providers
            .filterIsInstance(LifecycleAwareProvider::class.java)
            .forEach {
                it.onActivityStop(activityContext)
            }
    }
}
