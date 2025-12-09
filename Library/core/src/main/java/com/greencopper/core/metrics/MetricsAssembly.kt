package com.greencopper.core.metrics

import com.greencopper.core.metrics.provider.FirebaseProvider
import com.greencopper.core.metrics.provider.FirebaseServiceSwitch
import com.greencopper.core.metrics.service.*
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.di.resolver.resolveAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class MetricsAssembly: Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<AggregateMetricsService> {
                ConcreteAggregateMetricsService(
                    resolveAll(allowSubclasses = true),
                    CoroutineScope(Dispatchers.Default),
                )
            }
            bindSingleton(auto(::MappedMetadataService))
            bindSingleton {
                FirebaseProvider(
                    contentManager = resolve(),
                    localStorage = resolve(),
                    context = resolve(),
                    coroutineScope = CoroutineScope(Dispatchers.IO),
                    buildConfigProvider = resolve(),
                )
            }
            bindSingleton(auto(::FirebaseServiceSwitch))
        }
    }
}
