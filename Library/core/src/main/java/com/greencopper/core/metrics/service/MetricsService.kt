package com.greencopper.core.metrics.service

import com.greencopper.core.metrics.Metrics

public interface MetricsService {
    public fun track(metrics: Metrics)
}