package com.greencopper.ticketing

import com.greencopper.ticketing.providers.DIProviderResolver
import com.greencopper.ticketing.providers.ProviderResolver
import com.greencopper.ticketing.providers.showclix.ShowclixAssembly
import com.greencopper.ticketing.ticketsscan.TicketsScanAssembly
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider

public class TicketingAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindAssembly(ShowclixAssembly())
            bindAssembly(TicketsScanAssembly())

            bindProvider<ProviderResolver>{ DIProviderResolver() }
        }
    }
}