package com.greencopper.toolkit.appinstance

import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.Resolver

internal class TestAssembly: Assembly {
    internal var isRegistered = false

    override fun registerBindings(registrar: Registrar) {

    }

    override fun onBindingsRegistered(resolver: Resolver) {
        isRegistered = true
    }
}
