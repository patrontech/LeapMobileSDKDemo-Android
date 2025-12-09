package com.greencopper.core.location

import com.greencopper.core.CoreAssembly
import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.core.location.conditions.LocationPermissionsCondition
import com.greencopper.core.location.manager.ConcreteLocationManager
import com.greencopper.core.location.manager.LocationManager
import com.greencopper.core.location.recipe.LocationRecipe
import com.greencopper.core.location.recipe.LocationRecipeOverride
import com.greencopper.core.location.service.ConcreteLocationService
import com.greencopper.core.location.service.LocationService
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class LocationAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindCondition(LocationRegionCondition.key, auto(::LocationRegionCondition))
            bindRecipe(auto(::LocationRecipe))
            bindRecipeOverride(auto(::LocationRecipeOverride))
            bindSingleton(auto(::LocationConfigurationHolder))
            bindSingleton<LocationManager> {
                ConcreteLocationManager(
                    geolocationProvider = resolve(),
                    remoteStateDispatcher = resolve(),
                    lazyLocalStorage = lazyResolver(),
                    scope = CoroutineScope(Dispatchers.IO)
                )
            }
            bindSingleton<LocationService> {
                ConcreteLocationService(
                    context = resolve(),
                    permissionManager = resolve(),
                    locationManager = resolve(),
                    locationConfigurationHolder = resolve(),
                    currentProjectTagProvider = resolve(),
                    versionProvider = resolve(),
                    localizationService = resolve(),
                    lazyLocalStorage = lazyResolver(),
                    scope = resolve(tag = CoreAssembly.singleThreadScopeTag),
                )
            }
            bindCondition(LocationPermissionsCondition.key, auto(::LocationPermissionsCondition))
        }
    }
}
