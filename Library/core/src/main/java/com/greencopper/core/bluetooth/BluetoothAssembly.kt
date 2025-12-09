package com.greencopper.core.bluetooth

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class BluetoothAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<BluetoothService> {
                ConcreteBluetoothService(
                    resolve(),
                    resolve(),
                    scope = CoroutineScope(Dispatchers.Main)
                )
            }
            bindCondition(BluetoothPermissionCondition.key, auto(::BluetoothPermissionCondition))
        }
    }

    override fun onBindingsRegistered(resolver: Resolver) {
        resolver.resolve<BluetoothService>()
    }
}
