package com.greencopper.core.automation

import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.testmocks.toolkit.MockResolver
import com.greencopper.toolkit.di.container.Key
import com.greencopper.toolkit.di.resolver.ResolveException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class AutomationRunnerTest {

    private var automationsDI: Map<AutomationKey, MockAutomation> = emptyMap()

    private val mockResolver = MockResolver { key, tag, params ->
        automationsDI[tag]?.let { automation ->
            Pair(Key(Automation::class, tag), automation)
        } ?: throw ResolveException(Key(Automation::class, tag))
    }

    private var automationRunner: AutomationRunner = ConcreteAutomationRunner(
        mockResolver,
        MockLogging()
    )

    @Test
    fun setupValidAutomations_shouldHaveThemRunning() {
        val a1 = MockAutomation()
        val a2 = MockAutomation()
        val key1 = AutomationKey("a1", 1)
        val key2 = AutomationKey("a2", 1)
        automationsDI = mapOf(
            key1 to a1,
            key2 to a2,
        )

        automationRunner.setup(
            listOf(
                AutomationInfo(AutomationKey("a1", 1)),
                AutomationInfo(AutomationKey("a2", 1)),
            )
        )

        assert(a1.didSetup)
        assert(a2.didSetup)
    }

    @Test
    fun setupUnknownAutomation_shouldExcludeThem() {
        val a1 = MockAutomation()
        val a2 = MockAutomation()
        val key1 = AutomationKey("a1", 1)
        val key2 = AutomationKey("a2", 1)
        automationsDI = mapOf(
            key2 to a2,
        )

        automationRunner.setup(
            listOf(
                AutomationInfo(key1),
                AutomationInfo(key2),
            )
        )

        assertFalse(a1.didSetup)
        assert(a2.didSetup)
    }

    @Test
    fun setupThrowingAutomation_shouldExcludeThem() {
        val a1 = MockAutomation { throw RuntimeException() }
        val a2 = MockAutomation()
        val key1 = AutomationKey("a1", 1)
        val key2 = AutomationKey("a2", 1)
        automationsDI = mapOf(
            key2 to a2,
        )

        automationRunner.setup(
            listOf(
                AutomationInfo(key1),
                AutomationInfo(key2),
            )
        )

        assertFalse(a1.didSetup)
        assert(a2.didSetup)
    }

    private class MockAutomation(
        val _setup: (params: AutomationParams?) -> Unit = {},
    ) : Automation {
        var didSetup = false
        override fun setup(params: AutomationParams?) {
            didSetup = true
            _setup(params)
        }
    }
}
