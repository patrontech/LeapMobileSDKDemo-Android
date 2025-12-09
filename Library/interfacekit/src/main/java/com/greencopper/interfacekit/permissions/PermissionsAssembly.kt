package com.greencopper.interfacekit.permissions

import com.greencopper.core.permissions.PermissionManager
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class PermissionsAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<PermissionManager> {
                ConcretePermissionManager(
                    resolve(),
                    resolve(),
                    lazyResolver(),
                    CoroutineScope(Dispatchers.IO),
                )
            }
        }
    }
}