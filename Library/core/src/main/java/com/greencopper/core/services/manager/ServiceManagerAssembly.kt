package com.greencopper.core.services.manager

import com.greencopper.core.CoreAssembly
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.*

internal class ServiceManagerAssembly: Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<ServiceManager> {
                ConcreteServiceManager(
                    resolveAll(allowSubclasses = true),
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(tag = CoreAssembly.singleThreadScopeTag),
                )
            }
            bindSingleton(auto(::ServicesConfigurationHolder))
            bindRecipe(auto(::ServicesConfigurationRecipe))
        }
    }

    override fun onBindingsRegistered(resolver: Resolver) {
        resolver.resolve<ServiceManager>()
    }
}
