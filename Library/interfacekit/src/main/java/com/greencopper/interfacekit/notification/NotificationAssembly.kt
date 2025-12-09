package com.greencopper.interfacekit.notification

import com.greencopper.interfacekit.commands.system.bindCommand
import com.greencopper.interfacekit.notification.command.NotificationsSettingsCommand
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto

internal class NotificationAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindCommand(NotificationsSettingsCommand.commandInfo.key, auto(::NotificationsSettingsCommand))
        }
    }
}
