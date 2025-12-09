package com.greencopper.thuzi.logout

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.utils.StoreCoroutineProvider
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.resolver.resolve
import com.toggl.komposable.extensions.combine
import com.toggl.komposable.extensions.createStore

internal class LogoutAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        with (registrar) {
            bindFeature(LogoutInitializer.key, auto(::LogoutInitializer))
            bindViewModel {
                val storeCoroutineProvider = resolve<StoreCoroutineProvider>()
                LogoutViewModel(
                    viewBuilder = resolve(),
                    store = createStore(
                        initialState = LogoutState(),
                        reducer = combine(
                            LogoutReducer(
                                localizationService = resolve<LocalizationService>(),
                            ),
                            LogoutAnalyticsReducer(
                                metricsService = resolve(),
                            ),
                        ),
                        storeScopeProvider = storeCoroutineProvider.storeScopeProvider,
                        dispatcherProvider = storeCoroutineProvider.dispatcherProvider,
                    ),
                    registrationManager = resolve(),
                    rootLayoutManager = resolve(),
                )
            }
        }
    }
}
