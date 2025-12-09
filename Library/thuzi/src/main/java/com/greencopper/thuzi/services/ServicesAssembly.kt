package com.greencopper.thuzi.services

import com.greencopper.thuzi.services.attendee.AttendeeService
import com.greencopper.thuzi.services.attendee.ConcreteAttendeeService
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class ServicesAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<AttendeeService> {
                ConcreteAttendeeService(
                    lazyLocalStorage = lazyResolver(),
                    registrationManager = resolve(),
                    remoteStateDispatcher = resolve(),
                    thuziAPI = resolve(),
                    registrationConfigurationHolder = resolve(),
                    currentProjectTagProvider = resolve(),
                    scope = CoroutineScope(Dispatchers.Default),
                    contentManager = resolve(),
                    logging = resolve(),
                )
            }
        }
    }
}
