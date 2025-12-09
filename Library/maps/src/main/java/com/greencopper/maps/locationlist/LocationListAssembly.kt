package com.greencopper.maps.locationlist

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.maps.common.MyLocationsManager
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.resolve

internal class LocationListAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {

            bindFeature(LocationListInitializer.key) {
                LocationListInitializer()
            }
            bindFeature(LocationsListV2Initializer.key, auto(::LocationsListV2Initializer))
            bindProvider<ListProvider>(tag = LocationsListProvider.key) {
                LocationsListProvider(resolve())
            }

            bindViewModel { params ->
                LocationListViewModel(
                    resolve(),
                    resolve(),
                    resolve(args = params.toArray()),
                    resolve(),
                    resolve(tag = MyLocationsManager.diKey),
                    App.locale
                )
            }
        }
    }
}
