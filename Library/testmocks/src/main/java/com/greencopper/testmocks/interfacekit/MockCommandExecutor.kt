package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.commands.system.CommandExecutor
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockCommandExecutor : CommandExecutor {
    public var executedCommandInfo: CommandInfo? = null
    public var executedLayout: Layout? = null

    public var shouldThrow: Boolean = false

    override fun execute(commandInfo: CommandInfo, origin: Layout?): Flow<Boolean> {
        if (shouldThrow) {
            throw IllegalArgumentException("Unable to resolve command: ${commandInfo.key}")
        } else {
            executedCommandInfo = commandInfo
            executedLayout = origin
        }

        return flowOf(true)
    }

    override fun executeAsync(commandInfo: CommandInfo, origin: Layout?) {
        if (shouldThrow) {
            throw IllegalArgumentException("Unable to resolve command: ${commandInfo.key}")
        } else {
            executedCommandInfo = commandInfo
            executedLayout = origin
        }
    }
}
