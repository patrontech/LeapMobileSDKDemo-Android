package com.greencopper.core.metrics.provider

public interface MetricsProvider {
    public val name: Provider
    public fun enable()
    public fun disable()
}