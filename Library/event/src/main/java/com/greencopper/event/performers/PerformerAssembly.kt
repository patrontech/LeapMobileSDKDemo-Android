package com.greencopper.event.performers

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.event.activity.MyActivitiesManager
import com.greencopper.event.performers.conditions.IsInMyPerformersCondition
import com.greencopper.event.performers.data.repository.DatabasePerformerRepository
import com.greencopper.event.performers.data.repository.PerformerRepository
import com.greencopper.event.performers.viewmodel.PerformerDetailViewModel
import com.greencopper.event.performers.viewmodel.PerformersListViewModel
import com.greencopper.event.performers.widgets.PerformerCollectionWidgetInitializer
import com.greencopper.event.scheduleItem.MyScheduleManager
import com.greencopper.event.searchProvider.PerformersSearchProvider
import com.greencopper.interfacekit.bindCounter
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.search.logic.SearchProvider
import com.greencopper.interfacekit.widgets.initializer.bindWidget
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.Dispatchers

internal class PerformerAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<PerformerRepository> {
                DatabasePerformerRepository(
                    resolve(),
                    Dispatchers.IO
                )
            }

            bindProvider<FavoritesManager<*>>(tag = MyPerformersManager.diKey) {
                MyPerformersManager(
                    remoteStateDispatcher = resolve(),
                    lazyLocalStorage = lazyResolver(),
                    performersRepository = resolve(),
                )
            }
            bindFeature(PerformersListInitializer.key, auto(::PerformersListInitializer))
            bindFeature(PerformersListV2Initializer.key, auto(::PerformersListV2Initializer))
            bindProvider<ListProvider>(tag = PerformersListProvider.key) {
                PerformersListProvider(resolve())
            }

            bindViewModel { params ->
                PerformersListViewModel(
                    resolve(),
                    resolve(args = params.toArray()),
                    resolve(),
                    resolve(),
                    resolve(tag = MyPerformersManager.diKey),
                    App.locale,
                )
            }

            bindFeature(PerformerDetailInitializer.key, auto(::PerformerDetailInitializer))
            bindViewModel {
                PerformerDetailViewModel(
                    performerRepository = resolve(),
                    timedScheduleItemRepository = resolve(),
                    localizationService = resolve(),
                    myScheduleManager = resolve(tag = MyScheduleManager.diKey),
                    myActivitiesManager = resolve(tag = MyActivitiesManager.diKey),
                    myPerformersManager = resolve(tag = MyPerformersManager.diKey),
                    widgetResolver = resolve(),
                    widgetCollectionResolver = resolve(),
                )
            }

            bindWidget(PerformerCollectionWidgetInitializer.key, auto(::PerformerCollectionWidgetInitializer))

            bindProvider<SearchProvider>(
                tag = PerformersSearchProvider.key,
                auto(::PerformersSearchProvider)
            )

            bindCondition(IsInMyPerformersCondition.key) {
                IsInMyPerformersCondition(resolve(tag = MyPerformersManager.diKey))
            }
            bindCounter(MyPerformersCounter.key) {
                MyPerformersCounter(it[0], resolve(tag = MyPerformersManager.diKey), resolve())
            }
        }
    }
}
