package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.commands.system.*

public class MockCommandResolver : CommandResolver {
    public val commands: HashMap<CommandInfo.Key, Command> = HashMap<CommandInfo.Key, Command>()
    override fun resolve(key: CommandInfo.Key): Command? =
        commands[key]
}

public fun MockCommandResolver.bindCommand(key: CommandInfo.Key, getCommand: () -> Command) =
    commands.put(key, getCommand())
