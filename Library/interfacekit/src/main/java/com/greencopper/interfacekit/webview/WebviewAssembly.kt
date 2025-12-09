package com.greencopper.interfacekit.webview

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.di.resolver.resolveAll

internal class WebviewAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(WebviewInitializer.key, auto(::WebviewInitializer))
            bindViewModel {
                BaseWebViewViewModel(
                    buildConfigProvider = resolve(),
                    routeController = resolve(),
                    permissionManager = resolve(),
                    localizationService = resolve(),
                    webviewClientListeners = resolveAll(allowSubclasses = true),
                    appContext = resolve(),
                    logging = resolve()
                )
            }
        }
    }
}
