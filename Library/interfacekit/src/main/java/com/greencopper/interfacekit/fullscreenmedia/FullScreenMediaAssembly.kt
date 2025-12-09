package com.greencopper.interfacekit.fullscreenmedia

import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto

internal class FullScreenMediaAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(FullScreenMediaInitializer.key, auto(::FullScreenMediaInitializer))
        }
    }
}
