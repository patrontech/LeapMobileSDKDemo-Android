package com.greencopper.core.asset

import com.greencopper.core.asset.manager.*
import com.greencopper.core.asset.recipe.*
import com.greencopper.core.networking.CoreAPI
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.httpclient.APIProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

internal class AssetsAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton { AssetsConfigurationHolder() }
            bindRecipe(auto(::AssetsRecipe))
            bindRecipeOverride(auto(::AssetsRecipeOverride))
            bindProvider<AssetsStorageManager>(auto(::ConcreteAssetsStorageManager))
            bindProvider<AssetsManager> {
                ConcreteAssetsManager(
                    coreAPI = resolve<APIProvider<CoreAPI>>().api(),
                    assetsStorageManager = resolve(),
                    assetsConfigurationHolder = resolve(),
                    logger = resolve(),
                    coroutineScope = CoroutineScope(
                        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher()
                    ),
                )
            }
        }
    }
}
