package com.greencopper.thuzi.microsite

import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.resolve

internal class MicrositeAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(MicrositeInitializer.key) {
                MicrositeInitializer(
                    localStorage = resolve(),
                    localizationService = resolve(),
                )
            }
        }
    }
}
