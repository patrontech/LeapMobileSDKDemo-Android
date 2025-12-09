package com.greencopper.core.services.manager

import com.greencopper.core.metrics.provider.MetricsProvider

public abstract class MetricsServiceSwitch<P : MetricsProvider>(
    private val provider: P,
) : ServiceSwitch {
    override val aspect: ServiceAspect = provider.name.serviceAspect

    override fun enable(enabled: Boolean) {
        if (enabled) {
            provider.enable()
        } else {
            provider.disable()
        }
    }
}
