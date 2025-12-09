package com.greencopper.thuzi.survey

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.resolver.resolve

internal class SurveyAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(SurveyInitializer.key) {
                SurveyInitializer(
                    localizationService = resolve(),
                    localStorage = resolve()
                )
            }
            bindViewModel(auto(::SurveyViewModel))
        }
    }
}
