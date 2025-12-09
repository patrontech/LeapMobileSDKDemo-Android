package com.greencopper.event.activity

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.event.activity.conditions.IsInMyActivitiesCondition
import com.greencopper.event.activity.data.repository.ActivityRepository
import com.greencopper.event.activity.data.repository.DatabaseActivityRepository
import com.greencopper.event.activity.viewmodel.ActivitiesListViewModel
import com.greencopper.event.activity.viewmodel.ActivityDetailViewModel
import com.greencopper.event.scheduleItem.MyScheduleManager
import com.greencopper.event.searchProvider.ActivitiesSearchProvider
import com.greencopper.interfacekit.bindCounter
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.search.logic.SearchProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.Dispatchers

internal class ActivityAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<ActivityRepository> {
                DatabaseActivityRepository(
                    resolve(),
                    Dispatchers.IO
                )
            }
            bindViewModel { params ->
                ActivitiesListViewModel(
                    resolve(),
                    resolve(args = params.toArray()),
                    resolve(),
                    resolve(),
                    resolve(tag = MyActivitiesManager.diKey),
                    App.locale,
                )
            }
            bindViewModel {
                ActivityDetailViewModel(
                    activityRepository = resolve(),
                    timedScheduleItemRepository = resolve(),
                    localizationService = resolve(),
                    myScheduleManager = resolve(tag = MyScheduleManager.diKey),
                    myActivitiesManager = resolve(tag = MyActivitiesManager.diKey),
                    widgetResolver = resolve(),
                    widgetCollectionResolver = resolve(),
                )
            }
            bindFeature(ActivityDetailInitializer.key, auto(::ActivityDetailInitializer))

            bindFeature(ActivitiesListInitializer.key, auto(::ActivitiesListInitializer))
            bindFeature(ActivitiesListV2Initializer.key, auto(::ActivitiesListV2Initializer))
            bindProvider<ListProvider>(tag = ActivitiesListProvider.key) {
                ActivitiesListProvider(resolve())
            }

            bindSingleton<FavoritesManager<*>>(tag = MyActivitiesManager.diKey) {
                MyActivitiesManager(
                    lazyLocalStorage = lazyResolver(),
                    remoteStateDispatcher = resolve(),
                    activitiesRepository = resolve(),
                )
            }

            bindProvider<SearchProvider>(
                tag = ActivitiesSearchProvider.key,
                auto(::ActivitiesSearchProvider)
            )

            bindCondition(IsInMyActivitiesCondition.key) {
                IsInMyActivitiesCondition(resolve(tag = MyActivitiesManager.diKey))
            }

            bindCounter(MyActivitiesCounter.key) {
                MyActivitiesCounter(it[0], resolve(tag = MyActivitiesManager.diKey), resolve())
            }
        }
    }
}
