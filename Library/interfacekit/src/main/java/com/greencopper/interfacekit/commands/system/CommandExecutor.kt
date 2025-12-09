package com.greencopper.interfacekit.commands.system

import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public interface CommandExecutor {
    @Throws(Throwable::class)
    public fun execute(commandInfo: CommandInfo, origin: Layout?): Flow<Boolean>

    public fun executeAsync(commandInfo: CommandInfo, origin: Layout?)
}

internal class ConcreteCommandExecutor(
    private val commandResolver: CommandResolver,
    private val scope: CoroutineScope,
) : CommandExecutor {
    private fun resolveCommand(commandInfo: CommandInfo): Command =
        commandResolver.resolve(commandInfo.key)
            ?: throw IllegalArgumentException("Unable to resolve command: ${commandInfo.key}")

    @Throws(Throwable::class)
    override fun execute(commandInfo: CommandInfo, origin: Layout?) =
        resolveCommand(commandInfo)
            .execute(commandInfo.params, origin)

    override fun executeAsync(commandInfo: CommandInfo, origin: Layout?) {
        val command = resolveCommand(commandInfo)
        scope.launch {
            try {
                command.execute(commandInfo.params, origin).collect()
            } catch (t: Throwable) {
                App.log.e("Command thrown exception", throwable = t)
            }
        }
    }
}
