package com.greencopper.interfacekit.commands.system

import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class CommandAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<CommandResolver> { DICommandResolver() }
            bindProvider<CommandExecutor> {
                ConcreteCommandExecutor(
                    commandResolver = App.resolve(),
                    scope = CoroutineScope(Dispatchers.IO)
                )
            }
        }
    }
}
