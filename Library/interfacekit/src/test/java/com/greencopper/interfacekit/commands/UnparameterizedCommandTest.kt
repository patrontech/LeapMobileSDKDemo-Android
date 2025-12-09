package com.greencopper.interfacekit.commands

import com.greencopper.interfacekit.commands.system.UnparameterizedCommand
import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UnparameterizedCommandTest {
    private class TestUnparameterizedCommand : UnparameterizedCommand() {
        var executed = false
        override fun execute(origin: Layout?): Flow<Boolean> {
            executed = true
            return flowOf(true)
        }
    }

    @Test
    fun whenCheckingCommandUnparameterized_WithNullParameters_shouldBeExecuted() {
        val unparameterizedCommand = TestUnparameterizedCommand()
        unparameterizedCommand.execute(null, null)
        assertThat(unparameterizedCommand.executed).isTrue
    }

    @Test
    fun whenCheckingCommandUnparameterized_WithJsonNullParameters_shouldBeExecuted() {
        val unparameterizedCommand = TestUnparameterizedCommand()
        unparameterizedCommand.execute(JsonNull)
        assertThat(unparameterizedCommand.executed).isTrue
    }

    @Test
    fun whenCheckingCommandUnparameterized_WithNoParameters_shouldBeExecuted() {
        val unparameterizedCommand = TestUnparameterizedCommand()
        unparameterizedCommand.execute()
        assertThat(unparameterizedCommand.executed).isTrue
    }
}
