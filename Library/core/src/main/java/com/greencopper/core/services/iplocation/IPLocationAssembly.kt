package com.greencopper.core.services.iplocation

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindRecipe
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class IPLocationAssembly: Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton(auto(::IPLocationConfigurationHolder))
            bindRecipe(auto(::IPLocationConfigRecipe))
            bindSingleton<IPLocationService> {
                ConcreteIPLocationService(
                    contentManager = resolve(),
                    coreAPIProvider = resolve(),
                    localStorage = resolve(),
                    configurationHolder = resolve(),
                    scope = CoroutineScope(Dispatchers.IO)
                )
            }
            bindCondition(
                IPLocationRestrictedAreaCondition.key,
            ) {
                IPLocationRestrictedAreaCondition(
                    resolve(),
                    resolve(),
                )
            }
        }
    }

    override fun onBindingsRegistered(resolver: Resolver) {
        resolver.resolve<IPLocationService>()
    }
}
