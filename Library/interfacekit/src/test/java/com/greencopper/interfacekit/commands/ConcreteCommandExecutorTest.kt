package com.greencopper.interfacekit.commands

import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.commands.system.ConcreteCommandExecutor
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.interfacekit.MockCommand
import com.greencopper.testmocks.interfacekit.MockCommandResolver
import com.greencopper.testmocks.interfacekit.bindCommand
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ConcreteCommandExecutorTest : CoroutineTest(StandardTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val commandResolver = MockCommandResolver()
    private val origin = mockk<Layout>()
    private val executor = ConcreteCommandExecutor(commandResolver, testScope)

    override fun afterEach() {}

    @Test
    fun whenUnknownCommand_shouldThrow() {
        val commandInfo = CommandInfo(CommandInfo.Key("Unknown", 1))
        assertThrows<IllegalArgumentException> { executor.execute(commandInfo, origin) }
    }

    @Test
    fun whenUnknownCommandWithNullOrigin_shouldThrow() {
        val commandInfo = CommandInfo(CommandInfo.Key("Unknown", 1))
        assertThrows<IllegalArgumentException> { executor.execute(commandInfo, null) }
    }

    @Test
    fun checkCommandExecutionOk_shouldNotThrow() {
        assertDoesNotThrow {
            val testCommand = MockCommand()
            commandResolver.bindCommand(MockCommand.key) { testCommand }
            executor.execute(MockCommand.commandInfo, origin)
            assertThat(testCommand.executed).isTrue
        }
    }

    @Test
    fun checkCommandExecutionOkWithNullOrigin_shouldNotThrow() {
        assertDoesNotThrow {
            val testCommand = MockCommand()
            commandResolver.bindCommand(MockCommand.key) { testCommand }
            executor.execute(MockCommand.commandInfo, null)
            assertThat(testCommand.executed).isTrue
        }
    }

    @Test
    fun whenUnknownCommandAsync_shouldThrow() {
        val commandInfo = CommandInfo(CommandInfo.Key("Unknown", 1))
        assertThrows<IllegalArgumentException> { executor.executeAsync(commandInfo, null) }
    }

    @Test
    fun testExecutionAsyncOk_shouldNotThrow() {
        assertDoesNotThrow {
            runTest {
                val testCommand = MockCommand()
                commandResolver.bindCommand(MockCommand.key) { testCommand }
                executor.executeAsync(MockCommand.commandInfo, null)
                delay(100)
                assertThat(testCommand.executed).isTrue
            }
        }
    }

    @Test
    fun testExecutionOfCommandThrowingException_shouldNotThrow() {
        assertDoesNotThrow {
            runTest {
                val testCommand = MockCommand()
                testCommand.customExecuteBlock = {
                    throw RuntimeException()
                }
                commandResolver.bindCommand(MockCommand.key) { testCommand }
                executor.executeAsync(MockCommand.commandInfo, null)
            }
        }
    }
}
