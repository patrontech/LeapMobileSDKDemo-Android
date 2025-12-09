package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.automation.AutomationRunner
import com.greencopper.event.recipe.EventConfigurationHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class MyScheduleAutomationRunner(
    private val automationRunner: AutomationRunner,
    private val configHolder: EventConfigurationHolder,
    private val scope: CoroutineScope,
) {
    init {
        scope.launch {
            configHolder.currentConfiguration.collectLatest { config ->
                config?.mySchedule?.automations?.let {
                    automationRunner.setup(it)
                }
            }
        }
    }
}
