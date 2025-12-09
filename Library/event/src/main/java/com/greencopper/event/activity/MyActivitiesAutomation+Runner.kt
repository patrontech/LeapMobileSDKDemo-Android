package com.greencopper.event.activity

import com.greencopper.core.automation.AutomationRunner
import com.greencopper.event.recipe.EventConfigurationHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class MyActivitiesAutomationRunner(
    automationRunner: AutomationRunner,
    configHolder: EventConfigurationHolder,
    scope: CoroutineScope,
) {
    init {
        scope.launch {
            configHolder.currentConfiguration.collectLatest { config ->
                config?.myActivities?.automations?.let {
                    automationRunner.setup(it)
                }
            }
        }
    }
}
