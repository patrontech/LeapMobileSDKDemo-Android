package com.greencopper.interfacekit.navigation

import com.greencopper.interfacekit.navigation.feature.DIFeatureResolver
import com.greencopper.interfacekit.navigation.feature.FeatureInitializer
import com.greencopper.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.route.ConcreteRouteController
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Creator
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.container.Key
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class NavigationAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<FeatureResolver>(auto(::DIFeatureResolver))
            bindProvider<RouteController> {
                ConcreteRouteController(
                    featureResolver = resolve(),
                    linkResolver = resolve(),
                    commandExecutor = resolve(),
                    context = resolve(),
                    rootLayout = resolve(),
                    localizationService = resolve(),
                    lazyLocalStorage = lazyResolver(),
                    mainThreadScope = CoroutineScope(Dispatchers.Main),
                    logger = resolve(),
                )
            }
        }
    }
}
