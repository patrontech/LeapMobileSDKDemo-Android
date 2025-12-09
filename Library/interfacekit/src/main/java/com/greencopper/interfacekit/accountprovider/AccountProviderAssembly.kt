package com.greencopper.interfacekit.accountprovider

import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.resolve

internal class AccountProviderAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<AccountProviderResolver> {
                DIAccountProviderResolver(App, resolve())
            }
        }
    }
}
