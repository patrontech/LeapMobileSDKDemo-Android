package com.greencopper.core.deferredcommand

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.di.resolver.resolveAll
import com.greencopper.toolkit.di.resolver.tryResolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext

internal class DeferredCommandAssembly: Assembly {
    @OptIn(DelicateCoroutinesApi::class)
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<DeferredCommandService> {
                ConcreteDeferredCommandService(
                    commands = resolveAll(allowSubclasses = true),
                    localStorage = resolve(),
                    logger = App.log,
                    networkMonitor = resolve(),
                    lifecycleOwner = ProcessLifecycleOwner.get(),
                    scope = CoroutineScope(Dispatchers.IO),
                    lock = newSingleThreadContext("DeferredCommandService")
                )
            }
        }
    }

    override fun onBindingsRegistered(resolver: Resolver) {
        resolver.tryResolve<Context>()?.let {
            // If we don't have a Context, then we're running
            // in the unit tests and don't want this service
            // started, because some of its dependencies
            // won't work in unit tests.
            resolver.resolve<DeferredCommandService>()
        }
    }
}
