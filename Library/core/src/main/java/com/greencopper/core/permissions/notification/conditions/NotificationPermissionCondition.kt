package com.greencopper.core.permissions.notification.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.core.permissions.notification.service.NotificationPermissionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class NotificationPermissionCondition(private val notificationPermissionService: NotificationPermissionService):
    ParameterizedCondition<NotificationPermissionCondition.NotificationPermissionConditionData>(){

    override fun checkWith(parameter: NotificationPermissionConditionData): Boolean =
        parameter.checkAuthorizationStatus(notificationPermissionService.getAuthorizationStatus())

    override fun checkWithFlow(parameter: NotificationPermissionConditionData): Flow<Boolean> =
        notificationPermissionService.getAuthorizationStatusFlow().map {
            parameter.checkAuthorizationStatus(it)
        }

    override fun deserialize(conditionParameters: ConditionParameters): NotificationPermissionConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    @Serializable
    internal data class NotificationPermissionConditionData(
        val authorizationStatus: String,
    ) :
        KiboSerializable<NotificationPermissionConditionData> {

        fun checkAuthorizationStatus(currentAuthorizationStatus: AuthorizationStatus): Boolean =
            when (authorizationStatus) {
                "authorized" -> AuthorizationStatus.AuthorizedAlways == currentAuthorizationStatus
                "denied" -> AuthorizationStatus.Denied == currentAuthorizationStatus
                else -> AuthorizationStatus.NotDetermined == currentAuthorizationStatus
            }

        override fun getSerializer(): KSerializer<NotificationPermissionConditionData> =
            serializer()
    }

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Core.Notification.Permissions", 1)
    }
}