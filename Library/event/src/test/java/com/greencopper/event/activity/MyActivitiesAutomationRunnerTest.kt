package com.greencopper.event.activity

import com.greencopper.core.automation.AutomationInfo
import com.greencopper.core.automation.AutomationKey
import com.greencopper.event.recipe.EventConfiguration
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockAutomationRunner
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MyActivitiesAutomationRunnerTest : CoroutineTest(StandardTestDispatcher()) {

    val configHolder = EventConfigurationHolder()

    val automationsRequested = mutableListOf<AutomationInfo>()
    val automationRunner = MockAutomationRunner { automationsRequested.addAll(it) }

    init {
        MyActivitiesAutomationRunner(
            automationRunner = automationRunner,
            configHolder = configHolder,
            scope = testScope
        )
    }

    override fun afterEach() {}

    @Test
    fun withValidConfiguration_shouldSetupAutomations() =
        runTest {
            val automations = listOf(
                AutomationInfo(AutomationKey("a1", 1)),
                AutomationInfo(AutomationKey("a2", 1)),
            )
            configHolder.currentConfiguration.value = EventConfiguration(
                myActivities = EventConfiguration.EventFeatureInfo(
                    automations = automations
                )
            )

            delay(500)

            assertThat(automationsRequested).containsAll(automations)
        }
}
