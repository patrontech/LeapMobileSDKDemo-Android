package com.greencopper.interfacekit.notification.command

import android.content.Context
import android.content.Intent
import com.greencopper.core.permissions.notification.service.NotificationPermissionService
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.commands.system.UnparameterizedCommand
import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class NotificationsSettingsCommand(
    private val context: Context,
    private val notificationPermissionService: NotificationPermissionService,
) : UnparameterizedCommand() {
    override fun execute(origin: Layout?): Flow<Boolean> {
        context.startActivity(
            notificationPermissionService.getSettingsIntent(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            null,
        )
        return flowOf(true)
    }

    companion object {
        val commandInfo = CommandInfo(CommandInfo.Key("InterfaceKit.Notifications.Settings", 1))
    }
}
