package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.commands.system.UnparameterizedCommand
import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockCommand : UnparameterizedCommand() {
    public companion object {
        public val commandInfo: CommandInfo = CommandInfo(
            CommandInfo.Key("TestCommand", 1)
        )
        public val key: CommandInfo.Key = commandInfo.key
    }

    public var executed: Boolean = false
    public var customExecuteBlock: () -> Unit = {}

    override fun execute(origin: Layout?): Flow<Boolean> {
        customExecuteBlock()
        executed = true
        return flowOf(true)
    }
}
