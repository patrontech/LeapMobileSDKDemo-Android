package com.greencopper.event.automation

import com.greencopper.core.automation.bindAutomation
import com.greencopper.event.activity.AddToMyScheduleIfOnlyOneScheduleItem
import com.greencopper.event.activity.MyActivitiesAutomationRunner
import com.greencopper.event.activity.MyActivitiesManager
import com.greencopper.event.activity.RemoveFromMyScheduleIfOnlyOneScheduleItem
import com.greencopper.event.scheduleItem.AddToMyActivitiesIfOnlyScheduleItem
import com.greencopper.event.scheduleItem.MyScheduleManager
import com.greencopper.event.scheduleItem.RemoveFromMyActivitiesIfOnlyScheduleItem
import com.greencopper.event.scheduleItem.viewmodel.MyScheduleAutomationRunner
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class AutomationAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindAutomation(AddToMyScheduleIfOnlyOneScheduleItem.key) {
                AddToMyScheduleIfOnlyOneScheduleItem(
                    myActivitiesManager = resolve(tag = MyActivitiesManager.diKey),
                    myScheduleManager = resolve(tag = MyScheduleManager.diKey),
                    scheduleItemRepository = resolve(),
                    scope = CoroutineScope(Dispatchers.IO),
                    logger = resolve(),
                )
            }
            bindAutomation(RemoveFromMyScheduleIfOnlyOneScheduleItem.key) {
                RemoveFromMyScheduleIfOnlyOneScheduleItem(
                    myActivitiesManager = resolve(tag = MyActivitiesManager.diKey),
                    myScheduleManager = resolve(tag = MyScheduleManager.diKey),
                    scheduleItemRepository = resolve(),
                    scope = CoroutineScope(Dispatchers.IO),
                    logger = resolve(),
                )
            }
            bindAutomation(AddToMyActivitiesIfOnlyScheduleItem.key) {
                AddToMyActivitiesIfOnlyScheduleItem(
                    myActivitiesManager = resolve(tag = MyActivitiesManager.diKey),
                    myScheduleManager = resolve(tag = MyScheduleManager.diKey),
                    scheduleItemRepository = resolve(),
                    scope = CoroutineScope(Dispatchers.IO),
                    logger = resolve(),
                )
            }
            bindAutomation(RemoveFromMyActivitiesIfOnlyScheduleItem.key) {
                RemoveFromMyActivitiesIfOnlyScheduleItem(
                    myActivitiesManager = resolve(tag = MyActivitiesManager.diKey),
                    myScheduleManager = resolve(tag = MyScheduleManager.diKey),
                    scheduleItemRepository = resolve(),
                    scope = CoroutineScope(Dispatchers.IO),
                    logger = resolve(),
                )
            }

            bindSingleton {
                MyScheduleAutomationRunner(
                    resolve(),
                    resolve(),
                    CoroutineScope(Dispatchers.IO)
                )
            }

            bindSingleton {
                MyActivitiesAutomationRunner(
                    resolve(),
                    resolve(),
                    CoroutineScope(Dispatchers.IO)
                )
            }
        }
    }
}
