package com.greencopper.thuzi.account

import com.greencopper.core.CoreAssembly
import com.greencopper.interfacekit.bindAccountProvider
import com.greencopper.thuzi.account.registration.RegistrationAssembly
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope

internal class AccountAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        with (registrar) {
            bindAssembly(RegistrationAssembly())

            bindAccountProvider(ThuziAccountProvider.key) {
                ThuziAccountProvider(
                    lazyLocalStorage = lazyResolver(),
                )
            }

            bindSingleton<DeviceSessionManager> {
                ConcreteDeviceSessionManager(
                    localStorage = resolve(),
                    singleThreadScope = resolve<CoroutineScope>(tag = CoreAssembly.singleThreadScopeTag),
                    currentProjectTagProvider = resolve(),
                )
            }
        }
    }
}
