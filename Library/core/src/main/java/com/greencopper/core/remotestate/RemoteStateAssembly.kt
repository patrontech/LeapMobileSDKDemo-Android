package com.greencopper.core.remotestate

import com.greencopper.core.networking.CoreAPI
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.httpclient.APIProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class RemoteStateAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.bindSingleton<RemoteStateDispatcher> {
            CMSRemoteStateDispatcher(
                coreAPI = resolve<APIProvider<CoreAPI>>().api(),
                signatureGenerator = resolve(),
                coreConfigurationHolder = resolve(),
                currentProjectTagProvider = resolve(),
                lazyLocalStorage = lazyResolver(),
                json = resolve(),
                backgroundScope = CoroutineScope(Dispatchers.IO),
            )
        }
    }
}
