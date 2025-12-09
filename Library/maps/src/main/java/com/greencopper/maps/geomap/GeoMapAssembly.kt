package com.greencopper.maps.geomap

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.resolver.resolve

internal class GeoMapAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(GeoMapInitializer.key, auto(::GeoMapInitializer))
            bindViewModel { params ->
                GeoMapViewModel(
                    geolocationProvider = resolve(),
                    locationService = resolve(),
                    featureResolver = resolve(),
                    filterHandler = resolve(args = params.toArray()),
                    imageService = resolve(),
                    localizationService = resolve(),
                )
            }
        }
    }
}
