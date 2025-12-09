package com.greencopper.core.services

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.metrics.Metrics
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.toolkit.appinstance.AppInstance
import com.greencopper.toolkit.di.resolver.resolve

public fun AppInstance.localizationService(): LocalizationService = resolve()
public fun AppInstance.track(metrics: Metrics): Unit = resolve<AggregateMetricsService>().track(metrics)
