package com.greencopper.maps

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.interfacekit.bindCounter
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.search.logic.SearchProvider
import com.greencopper.maps.common.IsInMyLocationsCondition
import com.greencopper.maps.common.MyLocationsCounter
import com.greencopper.maps.common.MyLocationsManager
import com.greencopper.maps.geomap.GeoMapAssembly
import com.greencopper.maps.locationdetail.LocationDetailAssembly
import com.greencopper.maps.locationlist.LocationListAssembly
import com.greencopper.maps.recipe.ConcreteMapsRepository
import com.greencopper.maps.recipe.MapsRecipe
import com.greencopper.maps.recipe.MapsRecipeOverride
import com.greencopper.maps.recipe.MapsRepository
import com.greencopper.maps.searchProvider.LocationsSearchProvider
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve

public class MapsAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindAssembly(GeoMapAssembly())
            bindAssembly(LocationDetailAssembly())
            bindAssembly(LocationListAssembly())
            bindSingleton<MapsRepository>(auto(::ConcreteMapsRepository))
            bindRecipe(auto(::MapsRecipe))
            bindRecipeOverride(auto(::MapsRecipeOverride))
            bindProvider<SearchProvider>(tag = LocationsSearchProvider.key, auto(::LocationsSearchProvider))
            bindProvider<FavoritesManager<String>>(tag = MyLocationsManager.diKey) {
                MyLocationsManager(resolve(), lazyResolver(), resolve())
            }
            bindCondition(IsInMyLocationsCondition.key) {
                IsInMyLocationsCondition(resolve())
            }
            bindCounter(MyLocationsCounter.key) {
                MyLocationsCounter(it[0], resolve(tag = MyLocationsManager.diKey), resolve())
            }
        }
    }
}
