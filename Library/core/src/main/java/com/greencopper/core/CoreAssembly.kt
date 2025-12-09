package com.greencopper.core

import com.greencopper.core.asset.AssetsAssembly
import com.greencopper.core.automation.AutomationRunner
import com.greencopper.core.automation.ConcreteAutomationRunner
import com.greencopper.core.bluetooth.BluetoothAssembly
import com.greencopper.core.conditions.ConditionsAssembly
import com.greencopper.core.content.ContentAssembly
import com.greencopper.core.deferredcommand.DeferredCommandAssembly
import com.greencopper.core.draftcontent.DraftContentAssembly
import com.greencopper.core.localization.LocalizationAssembly
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.LocalStorageAssembly
import com.greencopper.core.location.LocationAssembly
import com.greencopper.core.metrics.MetricsAssembly
import com.greencopper.core.networking.CoreAPI
import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.networking.SignatureGeneratorClient
import com.greencopper.core.notification.NotificationAssembly
import com.greencopper.core.permissions.notification.NotificationPermissionAssembly
import com.greencopper.core.recipe.CoreConfigRecipe
import com.greencopper.core.recipe.CoreConfigRecipeOverride
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.core.remotestate.RemoteStateAssembly
import com.greencopper.core.services.ConcreteRecipeRegisterService
import com.greencopper.core.services.RecipeRegisterService
import com.greencopper.core.services.iplocation.IPLocationAssembly
import com.greencopper.core.services.manager.ServiceManagerAssembly
import com.greencopper.core.timezone.ConcreteTimezoneProvider
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.httpclient.APIProvider
import com.greencopper.toolkit.httpclient.ConcreteAPIProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

public class CoreAssembly : Assembly {
    public companion object {
        public const val singleThreadScopeTag: String = "singleThreadScope"
    }

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton(tag = singleThreadScopeTag) {
                CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
            }
            bindSingleton { CoreConfigurationHolder() }
            bindAssembly(LocalStorageAssembly())
            bindAssembly(ConditionsAssembly())
            bindAssembly(ContentAssembly())
            bindAssembly(LocalizationAssembly())
            bindAssembly(LocationAssembly())
            bindAssembly(AssetsAssembly())
            bindAssembly(BluetoothAssembly())
            bindAssembly(MetricsAssembly())
            bindAssembly(NotificationAssembly())
            bindAssembly(RemoteStateAssembly())
            bindAssembly(NotificationPermissionAssembly())
            bindAssembly(IPLocationAssembly())
            bindAssembly(ServiceManagerAssembly())
            bindAssembly(DeferredCommandAssembly())
            bindAssembly(DraftContentAssembly())

            bindProvider<SignatureGenerator> {
                SignatureGeneratorClient(resolve(), resolve())
            }
            bindProvider<TimezoneProvider>(auto(::ConcreteTimezoneProvider))
            bindRecipe(auto(::CoreConfigRecipe))
            bindRecipeOverride(auto(::CoreConfigRecipeOverride))
            bindProvider<RecipeRegisterService>(auto(::ConcreteRecipeRegisterService))

            bindSingleton<APIProvider<CoreAPI>> {
                ConcreteAPIProvider(resolve(), resolve(), CoreAPI::class.java)
            }
            bindProvider<AutomationRunner> {
                ConcreteAutomationRunner(
                    resolver = this,
                    logger = resolve(),
                )
            }
        }
    }

    override fun onBindingsRegistered(resolver: Resolver) {
        //Generate the installationId to make sure it can be used in
        //URL replacements
        resolver.resolve<LocalStorage>().app.installationId
    }
}
