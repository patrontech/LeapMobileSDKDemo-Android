package com.greencopper.event.scheduleItem

import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.data.repository.TimedScheduleItemRepository
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.event.reminders.ui.ReminderUIManager
import com.greencopper.event.scheduleItem.conditions.IsInMyScheduleCondition
import com.greencopper.event.scheduleItem.data.repository.DatabaseScheduleItemRepository
import com.greencopper.event.scheduleItem.data.repository.ScheduleItemRepository
import com.greencopper.event.scheduleItem.ui.scheduledetail.ScheduleItemDetailInitializer
import com.greencopper.event.scheduleItem.viewmodel.*
import com.greencopper.interfacekit.bindCounter
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.interests.recipe.InterestsConfigurationHolder
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.utils.StoreCoroutineProvider
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import com.toggl.komposable.extensions.combine
import com.toggl.komposable.extensions.createStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class ScheduleAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<ScheduleItemRepository> {
                DatabaseScheduleItemRepository(
                    resolve(),
                    Dispatchers.IO
                )
            }
            bindViewModel { params ->
                val favoritesMode: FilteringHandler.Mode = params[0]
                val filteringInfoMap: Map<FilteringHandler.Mode, FilteringInfo?> = params[1]
                val scheduleData: ScheduleLayoutData = params[2]
                val initialState: ScheduleState = params[3]

                val localizationService = resolve<LocalizationService>()
                val filteringHandler = resolve<FilteringHandler>(args = arrayOf(favoritesMode, filteringInfoMap))
                val timezoneProvider = resolve<TimezoneProvider>()
                val storeCoroutineProvider = resolve<StoreCoroutineProvider>()
                val conditionChecker = resolve<ConditionChecker>()
                ScheduleListViewModel(
                    localizationService = localizationService,
                    filteringHandler = filteringHandler,
                    timezoneProvider = timezoneProvider,
                    scheduleData = scheduleData,
                    store = createStore(
                        initialState = initialState,
                        reducer = combine(
                            ScheduleReducer(
                                timedScheduleItemRepository = resolve<TimedScheduleItemRepository>(),
                                myScheduleManager = resolve<FavoritesManager<Long>>(tag = MyScheduleManager.diKey),
                                localizationService = localizationService,
                                filteringHandler = filteringHandler,
                                timezoneProvider = timezoneProvider,
                                widgetResolver = resolve<WidgetResolver>(),
                                eventConfigHolder = resolve<EventConfigurationHolder>(),
                                interestsConfigHolder = resolve<InterestsConfigurationHolder>(),
                                scheduleData = scheduleData,
                                routeController = resolve<RouteController>(),
                                reminderUIManager = resolve<ReminderUIManager>(),
                                localStorage = resolve<LocalStorage>(),
                                conditionChecker = conditionChecker,
                                json = resolve()
                            ),
                            ScheduleAnalyticsReducer(
                                metricsService = resolve<AggregateMetricsService>(),
                                filteringHandler = filteringHandler,
                                timezoneProvider = timezoneProvider,
                                scheduleData
                            )

                        ),
                        storeScopeProvider = storeCoroutineProvider.storeScopeProvider,
                        dispatcherProvider = storeCoroutineProvider.dispatcherProvider,
                    ),
                    scope = CoroutineScope(Dispatchers.IO),
                    conditionChecker = conditionChecker
                )
            }
            bindViewModel {
                ScheduleItemDetailViewModel(
                    scheduleItemRepository = resolve(),
                    timeSlotRepository = resolve(),
                    stageRepository = resolve(),
                    myScheduleManager = resolve(tag = MyScheduleManager.diKey),
                    widgetCollectionResolver = resolve(),
                    widgetResolver = resolve(),
                )
            }
            bindFeature(ScheduleItemDetailInitializer.key, auto(::ScheduleItemDetailInitializer))
            bindFeature(ScheduleInitializer.key, auto(::ScheduleInitializer))
            bindProvider<FavoritesManager<*>>(tag = MyScheduleManager.diKey) {
                MyScheduleManager(
                    scheduleItemRepository = resolve(),
                    remoteStateDispatcher = resolve(),
                    lazyLocalStorage = lazyResolver(),
                    reminderUIManager = resolve(),
                )
            }
            bindCounter(MyScheduleCounter.key) {
                MyScheduleCounter(it[0], resolve(), resolve(tag = MyScheduleManager.diKey), resolve())
            }
            bindCondition(IsInMyScheduleCondition.key) {
                IsInMyScheduleCondition(resolve(tag = MyScheduleManager.diKey))
            }
        }
    }
}
