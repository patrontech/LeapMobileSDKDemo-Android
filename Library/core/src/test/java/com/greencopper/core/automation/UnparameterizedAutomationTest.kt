package com.greencopper.core.automation

import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UnparameterizedAutomationTest {
    private class TestUnparameterizedAutomation : UnparameterizedAutomation() {
        var executed = false
        override fun setup() {
            executed = true
        }
    }

    @Test
    fun whenCheckingCommandUnparameterized_WithNullParameters_shouldBeExecuted() {
        val unparameterizedAutomation = TestUnparameterizedAutomation()
        unparameterizedAutomation.setup(null)
        assertThat(unparameterizedAutomation.executed).isTrue
    }

    @Test
    fun whenCheckingCommandUnparameterized_WithJsonNullParameters_shouldBeExecuted() {
        val unparameterizedAutomation = TestUnparameterizedAutomation()
        unparameterizedAutomation.setup(JsonNull)
        assertThat(unparameterizedAutomation.executed).isTrue
    }

    @Test
    fun whenCheckingCommandUnparameterized_WithNoParameters_shouldBeExecuted() {
        val unparameterizedAutomation = TestUnparameterizedAutomation()
        unparameterizedAutomation.setup()
        assertThat(unparameterizedAutomation.executed).isTrue
    }
}
