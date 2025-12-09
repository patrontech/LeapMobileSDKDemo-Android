package com.greencopper.thuzi.eventpass

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.thuzi.eventpass.initializer.EventPassInitializer
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.resolve

internal class EventPassAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindViewModel {
                EventPassViewModel(
                    localStorage = App.resolve()
                )
            }

            bindFeature(EventPassInitializer.key) {
                EventPassInitializer()
            }
        }
    }
}
