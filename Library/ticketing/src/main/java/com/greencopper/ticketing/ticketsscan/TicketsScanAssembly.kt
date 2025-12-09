package com.greencopper.ticketing.ticketsscan

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.ticketing.providers.ProviderResolver
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.Dispatchers

internal class TicketsScanAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(TicketsScanInitializer.key) {
                TicketsScanInitializer(resolve())
            }

            bindViewModel { params ->
                TicketsScanViewModel(
                    App.resolve<ProviderResolver>().resolve(params[0]),
                    Dispatchers.Default
                )
            }
        }
    }
}
