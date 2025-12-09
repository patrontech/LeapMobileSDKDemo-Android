package com.greencopper.event.reminders.ui

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.event.colors.EventColor
import com.greencopper.event.common.event
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.toolkit.di.resolver.LazyResolver

public interface ReminderUIManager {
    public fun onAddToMySchedule(origin: Layout? = null)
    public fun showReminderUI(origin: Layout? = null)
}

internal class ConcreteReminderUIManager(
    private val routeController: RouteController,
    private val linkResolver: LinkResolver,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val eventConfigurationHolder: EventConfigurationHolder,
) : ReminderUIManager {

    override fun onAddToMySchedule(origin: Layout?) {
        val eventStorage = lazyLocalStorage.resolve().project.event

        if (!eventStorage.firstEventAdded.value) {
            showReminderUI(origin)
            eventStorage.firstEventAdded.value = true
        }
    }

    override fun showReminderUI(origin: Layout?) {
        eventConfigurationHolder.currentConfiguration.value?.reminders?.onFirstAddToMyScheduleRouteLink
            ?.let { linkResolver.featureInfo(it) }
            ?.let { info ->
                routeController.openBottomSheet(origin, info, EventColor.schedule.reminders.background)
            }
    }
}
