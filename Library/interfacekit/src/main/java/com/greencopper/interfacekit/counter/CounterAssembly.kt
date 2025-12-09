package com.greencopper.interfacekit.counter

import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.resolve

internal class CounterAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<CounterResolver> {
                DICounterResolver(App, resolve())
            }
        }
    }
}
