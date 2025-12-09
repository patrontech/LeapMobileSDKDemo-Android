package com.greencopper.interfacekit.commands.system

import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.e

internal class DICommandResolver : CommandResolver {
    override fun resolve(key: CommandInfo.Key): Command? {
        return try {
            App.resolve(tag = key)
        } catch (t: Throwable) {
            App.log.e("Command was not resolved for $key: ${t.message}")
            null
        }
    }
}
