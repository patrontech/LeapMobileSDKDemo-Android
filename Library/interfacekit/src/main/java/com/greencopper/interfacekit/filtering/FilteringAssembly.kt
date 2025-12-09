package com.greencopper.interfacekit.filtering

import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider

internal class FilteringAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<FilteringHandler> { params ->
                ConcreteFilteringHandler(params[0], params[1])
            }
        }
    }
}
