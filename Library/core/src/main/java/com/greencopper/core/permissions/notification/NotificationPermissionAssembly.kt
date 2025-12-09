package com.greencopper.core.permissions.notification

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.core.permissions.notification.conditions.NotificationPermissionCondition
import com.greencopper.core.permissions.notification.service.ConcreteNotificationPermissionService
import com.greencopper.core.permissions.notification.service.NotificationPermissionService
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*

public class NotificationPermissionAssembly: Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindCondition(NotificationPermissionCondition.key, auto(::NotificationPermissionCondition))
            bindSingleton<NotificationPermissionService>(auto(::ConcreteNotificationPermissionService))
        }
    }
}
