package com.greencopper.interfacekit.commands

import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.commands.system.DICommandResolver
import com.greencopper.testmocks.bindCommand
import com.greencopper.testmocks.interfacekit.MockCommand
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.binding.auto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DICommandResolverTest {
    private val commandResolver = DICommandResolver()

    init {
        Toolkit.setupTest()
    }

    @Test
    fun whenResolvingExistingCommandPassed_shouldResolveProperCommand() {
        val testCommand = MockCommand()
        bindCommand(MockCommand.key) { testCommand }
        val command = commandResolver.resolve(MockCommand.key)
        assertThat(command).isEqualTo(testCommand)
    }

    @Test
    fun whenResolvingExistingCommand_shouldResolveProperCommand() {
        bindCommand(MockCommand.key, auto(::MockCommand))
        val command = commandResolver.resolve(MockCommand.key)
        assertThat(command).isNotNull
    }

    @Test
    fun whenResolvingNonExistingCommand_shouldResolveNull() {
        val command = commandResolver.resolve(CommandInfo.Key("Null", -1))
        assertThat(command).isNull()
    }
}
