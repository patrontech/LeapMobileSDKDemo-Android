package com.greencopper.thuzi.fanscan

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.resolve

internal class FanscanAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(FanscanInitializer.key) {
                FanscanInitializer()
            }

            bindViewModel {
                FanscanViewModel(
                    thuziAPI = resolve(),
                    permissionManager = resolve(),
                    localizationService = resolve(),
                    localStorage = resolve(),
                    linkResolver = resolve(),
                    routeController = resolve(),
                    registrationManager = resolve(),
                    currentProjectTagProvider = resolve(),
                    deferredCommandService = resolve(),
                    deviceSessionManager = resolve(),
                    logging = resolve(),
                )
            }

            bindProvider<KibaDecodeCallback>(auto(::ConcreteDecodeCallback))
            bindSingleton(auto(::FanscanDeferredCommand))
        }
    }
}
