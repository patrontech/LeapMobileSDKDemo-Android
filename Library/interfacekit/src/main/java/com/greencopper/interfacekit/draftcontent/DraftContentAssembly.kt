package com.greencopper.interfacekit.draftcontent

import com.greencopper.interfacekit.commands.system.bindCommand
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class DraftContentAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        with (registrar) {
            bindCommand(ToggleDraftContentCommand.key) {
                ToggleDraftContentCommand(
                    draftContentManager = resolve(),
                    routeController = resolve(),
                    localizationService = resolve(),
                    logging = resolve(),
                    backgroundScope = CoroutineScope(Dispatchers.IO),
                )
            }
        }
    }
}
