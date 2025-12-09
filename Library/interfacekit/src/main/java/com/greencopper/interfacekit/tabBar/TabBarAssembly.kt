package com.greencopper.interfacekit.tabBar

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.tabBar.viewmodel.TabBarAnalyticsReducer
import com.greencopper.interfacekit.tabBar.viewmodel.TabBarReducer
import com.greencopper.interfacekit.tabBar.viewmodel.TabBarState
import com.greencopper.interfacekit.tabBar.viewmodel.TabBarViewModel
import com.greencopper.interfacekit.utils.StoreCoroutineProvider
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.resolver.resolve
import com.toggl.komposable.extensions.combine
import com.toggl.komposable.extensions.createStore

internal class TabBarAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(TabBarInitializer.key, auto(::TabBarInitializer))
            bindViewModel { params ->
                val initialState: TabBarState = params[0]
                val storeCoroutineProvider = resolve<StoreCoroutineProvider>()

                TabBarViewModel(
                    viewBuilder = resolve(),
                    featureResolver = resolve(),
                    mappedMetadataService = resolve(),
                    store = createStore(
                        initialState = initialState,
                        reducer = combine(
                            TabBarReducer(
                                localizationService = resolve(),
                            ),
                            TabBarAnalyticsReducer(
                                metricsService = resolve(),
                            ),
                        ),
                        storeScopeProvider = storeCoroutineProvider.storeScopeProvider,
                        dispatcherProvider = storeCoroutineProvider.dispatcherProvider,
                    ),
                )
            }
        }
    }
}
