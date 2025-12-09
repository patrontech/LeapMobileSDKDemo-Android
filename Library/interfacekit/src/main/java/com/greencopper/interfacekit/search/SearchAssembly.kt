package com.greencopper.interfacekit.search

import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.search.viewmodel.SearchViewModel
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.resolver.resolve

internal class SearchAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(SearchInitializer.key, auto(::SearchInitializer))
            bindViewModel { params ->
                SearchViewModel(params[0], resolve<ConditionChecker>())
            }
        }
    }
}
