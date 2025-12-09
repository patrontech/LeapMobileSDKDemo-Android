package com.greencopper.core.notification

import com.greencopper.core.CoreAssembly
import com.greencopper.core.networking.CoreAPI
import com.greencopper.core.notification.notificationmanager.ConcreteNotificationManagerClient
import com.greencopper.core.notification.notificationmanager.NotificationManagerClient
import com.greencopper.core.notification.repository.ConcreteNotificationRepository
import com.greencopper.core.notification.repository.NotificationRepository
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.httpclient.APIProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public class NotificationAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<NotificationRepository> {
                ConcreteNotificationRepository(
                    notificationManager = resolve(),
                    coreConfigurationHolder = resolve(),
                    coreAPI = resolve<APIProvider<CoreAPI>>().api(),
                    signatureGenerator = resolve(),
                    secrets = resolve(),
                    currentProjectTagProvider = resolve(),
                    lazyLocalStorage = lazyResolver(),
                    context = resolve(),
                    recipeScope = resolve(tag = CoreAssembly.singleThreadScopeTag),
                    backgroundScope = CoroutineScope(Dispatchers.IO),
                    logging = resolve(),
                )
            }

            bindProvider<NotificationManagerClient> { ConcreteNotificationManagerClient(resolve()) }
        }
    }
}
