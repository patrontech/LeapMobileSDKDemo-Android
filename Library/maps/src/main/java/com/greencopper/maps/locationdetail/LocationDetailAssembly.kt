package com.greencopper.maps.locationdetail

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.maps.common.MyLocationsManager
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.resolve

internal class LocationDetailAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {

            bindFeature(LocationDetailInitializer.key) {
                LocationDetailInitializer()
            }

            bindViewModel {
                LocationDetailViewModel(resolve(), resolve(), resolve(), resolve(tag = MyLocationsManager.diKey))
            }
        }
    }
}
