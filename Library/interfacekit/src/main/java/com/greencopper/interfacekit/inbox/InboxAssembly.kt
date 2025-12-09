package com.greencopper.interfacekit.inbox

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public class InboxAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindViewModel {
                InboxViewModel(
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    CoroutineScope(Dispatchers.IO),
                )
            }
            bindFeature(InboxInitializer.key, auto(::InboxInitializer))
            bindProvider<InboxNotificationsRepository> {
                ConcreteInboxNotificationsRepository(
                    resolve(),
                    resolve(),
                    resolve(),
                    Dispatchers.IO
                )
            }
        }
    }
}
