package com.greencopper.interfacekit.list

import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.list.initializer.ListLayoutData
import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.interfacekit.list.viewmodel.ListAnalyticsReducer
import com.greencopper.interfacekit.list.viewmodel.ListReducer
import com.greencopper.interfacekit.list.viewmodel.ListState
import com.greencopper.interfacekit.list.viewmodel.ListViewModel
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.utils.StoreCoroutineProvider
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.resolve
import com.toggl.komposable.extensions.combine
import com.toggl.komposable.extensions.createStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class ListAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindViewModel { params ->
                val favoritesMode: FilteringHandler.Mode = params[0]
                val filteringInfoMap: Map<FilteringHandler.Mode, FilteringInfo?> = params[1]
                val listData: ListLayoutData = params[2]
                val initialState: ListState = params[3]

                val viewBuilder = resolve<IKViewBuilder>()
                val filteringHandler = resolve<FilteringHandler>(args = arrayOf(favoritesMode, filteringInfoMap))
                val storeCoroutineProvider = resolve<StoreCoroutineProvider>()
                val conditionChecker = resolve<ConditionChecker>()
                ListViewModel(
                    filteringHandler = filteringHandler,
                    viewBuilder = viewBuilder,
                    listData = listData,
                    conditionChecker = conditionChecker,
                    store = createStore(
                        initialState = initialState,
                        reducer = combine(
                            ListReducer.create(
                                listProvider = resolve<ListProvider>(tag = listData.providerKey),
                                favoritesManager = resolve<FavoritesManager<Any>>(tag = listData.favoritesManagerKey),
                                localizationService = viewBuilder.localizationService,
                                listData = listData,
                                widgetResolver = resolve<WidgetResolver>(),
                                conditionChecker = conditionChecker,
                                logger = resolve(),
                                routeController = resolve<RouteController>(),
                                interestsConfigHolder = resolve(),
                                localStorage = resolve(),
                                coroutineContext = Dispatchers.IO,
                                json = resolve(),
                            ),
                            ListAnalyticsReducer(
                                metricsService = resolve<AggregateMetricsService>(),
                                listData
                            )
                        ),
                        storeScopeProvider = storeCoroutineProvider.storeScopeProvider,
                        dispatcherProvider = storeCoroutineProvider.dispatcherProvider,
                    ),
                    scope = CoroutineScope(Dispatchers.IO),
                )

            }
        }
    }
}
