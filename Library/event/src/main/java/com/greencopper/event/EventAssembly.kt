package com.greencopper.event

import androidx.work.WorkManager
import com.greencopper.core.CoreAssembly
import com.greencopper.event.activity.ActivityAssembly
import com.greencopper.event.activity.MyActivitiesAutomationRunner
import com.greencopper.event.automation.AutomationAssembly
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.dao.RoomDatabaseHelper
import com.greencopper.event.data.repository.DatabaseTimedScheduleItemRepository
import com.greencopper.event.data.repository.TimedScheduleItemRepository
import com.greencopper.event.performers.PerformerAssembly
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.event.recipe.EventRecipe
import com.greencopper.event.recipe.EventRecipeOverride
import com.greencopper.event.reminders.ConcreteScheduleRemindersService
import com.greencopper.event.reminders.RemindersInitializer
import com.greencopper.event.reminders.ScheduleRemindersService
import com.greencopper.event.reminders.ui.ConcreteReminderUIManager
import com.greencopper.event.reminders.ui.ReminderUIManager
import com.greencopper.event.reminders.viewmodel.RemindersViewModel
import com.greencopper.event.scheduleItem.MyScheduleManager
import com.greencopper.event.scheduleItem.ScheduleAssembly
import com.greencopper.event.scheduleItem.viewmodel.MyScheduleAutomationRunner
import com.greencopper.event.stage.data.repository.DatabaseStageRepository
import com.greencopper.event.stage.data.repository.StageRepository
import com.greencopper.event.timeSlot.data.repository.DatabaseTimeSlotRepository
import com.greencopper.event.timeSlot.data.repository.TimeSlotRepository
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.Dispatchers

public class EventAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindAssembly(ScheduleAssembly())
            bindAssembly(ActivityAssembly())
            bindAssembly(PerformerAssembly())
            bindAssembly(AutomationAssembly())

            bindFeature(RemindersInitializer.key, auto(::RemindersInitializer))
            bindProvider<ReminderUIManager> {
                ConcreteReminderUIManager(
                    routeController = resolve(),
                    linkResolver = resolve(),
                    lazyLocalStorage = lazyResolver(),
                    eventConfigurationHolder = resolve(),
                )
            }
            bindProvider<TimeSlotRepository> {
                DatabaseTimeSlotRepository(
                    resolve(),
                    Dispatchers.IO
                )
            }
            bindProvider<StageRepository> { DatabaseStageRepository(resolve(), Dispatchers.IO) }
            bindProvider<TimedScheduleItemRepository> {
                DatabaseTimedScheduleItemRepository(
                    resolve(),
                    Dispatchers.IO
                )
            }
            bindSingleton<DatabaseHelper> {
                RoomDatabaseHelper(
                    context = App.resolve(),
                    eventDatabaseScope = App.resolve(tag = CoreAssembly.singleThreadScopeTag),
                )
            }
            bindProvider<EventDataProcessor> {
                RoomEventDataProcessor(
                    jsonParser = App.resolve(),
                    context = App.resolve(),
                    databaseHelper = App.resolve(),
                    eventDatabaseScope = App.resolve(tag = CoreAssembly.singleThreadScopeTag),
                )
            }
            bindRecipe(auto(::EventRecipe))
            bindRecipeOverride(auto(::EventRecipeOverride))
            bindSingleton { EventConfigurationHolder() }
            bindSingleton<ScheduleRemindersService> {
                ConcreteScheduleRemindersService(
                    workManager = WorkManager.getInstance(resolve()),
                    scope = resolve(tag = CoreAssembly.singleThreadScopeTag),
                    currentProjectTagProvider = resolve(),
                    timezoneProvider = resolve(),
                    localizationService = resolve(),
                    eventConfigHolder = resolve(),
                    timedScheduleItemRepository = resolve(),
                    lazyLocalStorage = lazyResolver(),
                    notificationPermissionService = resolve(),
                    myScheduleManager = resolve(tag = MyScheduleManager.diKey),
                )
            }
            bindViewModel {
                RemindersViewModel(
                    configHolder = resolve(),
                    lazyLocalStorage = lazyResolver(),
                    reminderService = resolve(),
                    notificationPermissionService = resolve(),
                    localizationService = resolve(),
                    notificationManager = resolve(),
                )
            }
        }
    }

    override fun onBindingsRegistered(resolver: Resolver) {
        resolver.resolve<ScheduleRemindersService>().collectScheduleReminders()

        resolver.resolve<MyScheduleAutomationRunner>()
        resolver.resolve<MyActivitiesAutomationRunner>()
    }
}
