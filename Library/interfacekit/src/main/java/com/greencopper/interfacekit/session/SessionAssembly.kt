package com.greencopper.interfacekit.session

import com.greencopper.core.CoreAssembly
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve

public class SessionAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<SessionManager> {
                ConcreteSessionManager(
                    configHolder = resolve(),
                    contentInitializer = resolve(),
                    contentManager = resolve(),
                    draftContentManager = resolve(),
                    lazyOTAManager = lazyResolver(),
                    routeController = resolve(),
                    rootLayoutManager = resolve(),
                    backgroundCoroutineScope = resolve(tag = CoreAssembly.singleThreadScopeTag),
                    logger = resolve(),
                )
            }
        }
    }
}
