package com.greencopper.interfacekit.rootview

import com.greencopper.core.CoreAssembly
import com.greencopper.interfacekit.rootview.recipe.RootViewRecipe
import com.greencopper.interfacekit.rootview.recipe.RootViewRecipeOverride
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.resolve

internal class RootViewAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton { RootViewConfigurationHolder() }
            bindProvider { RootLayoutHolder() }
            bindRecipe(auto(::RootViewRecipe))
            bindRecipeOverride(auto(::RootViewRecipeOverride))
            bindProvider(auto(::RootViewRecipe))
            //RootLayoutManager cannot be instanced multiple times, otherwise it will trigger root layout
            //updates for one content update.
            bindSingleton<RootLayoutManager> {
                ConcreteRootLayoutManager(
                    featureResolver = resolve(),
                    onboardingConfigHolder = resolve(),
                    rootLayoutHolder = resolve(),
                    rootViewConfigurationHolder = resolve(),
                    currentProjectTagProvider = resolve(),
                    conditionChecker = resolve(),
                    defaultScope = resolve(tag = CoreAssembly.singleThreadScopeTag)
                )
            }
            bindSingleton { RootLayoutHolder.rootLayoutHolder }
        }
    }
}
