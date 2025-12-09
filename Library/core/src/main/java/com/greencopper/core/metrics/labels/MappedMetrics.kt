package com.greencopper.core.metrics.labels

import com.greencopper.core.metrics.Metrics
import com.greencopper.core.metrics.provider.MappedProvider

public interface MappedMetrics : Metrics {
    public fun track(provider: MappedProvider)
}