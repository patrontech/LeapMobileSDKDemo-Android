package com.greencopper.interfacekit.sample

import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto

internal class SampleAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(SampleFeatureInitializer.key, auto(::SampleFeatureInitializer))
        }
    }
}
