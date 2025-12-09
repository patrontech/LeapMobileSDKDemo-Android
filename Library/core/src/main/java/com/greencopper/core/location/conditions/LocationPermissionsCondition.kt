package com.greencopper.core.location.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.location.conditions.LocationPermissionsCondition.LocationPermissionsConditionData
import com.greencopper.core.location.service.LocationService
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.core.permissions.AuthorizationStatus.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class LocationPermissionsCondition(private val locationService: LocationService) :
    ParameterizedCondition<LocationPermissionsConditionData>() {

    override fun checkWith(parameter: LocationPermissionsConditionData): Boolean =
        parameter.checkAuthorizationStatus(locationService.getAuthorizationStatus())

    override fun checkWithFlow(parameter: LocationPermissionsConditionData): Flow<Boolean> =
        locationService.getAuthorizationStatusFlow().map {
            parameter.checkAuthorizationStatus(it)
        }

    override fun deserialize(conditionParameters: ConditionParameters): LocationPermissionsConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    @Serializable
    internal data class LocationPermissionsConditionData(
        val authorizationStatus: String,
    ) :
        KiboSerializable<LocationPermissionsConditionData> {

        fun checkAuthorizationStatus(currentAuthorizationStatus: AuthorizationStatus): Boolean =
            when (authorizationStatus) {
                "whenInUse" -> AuthorizedWhenInUse == currentAuthorizationStatus || AuthorizedAlways == currentAuthorizationStatus
                "always" -> AuthorizedAlways == currentAuthorizationStatus
                "denied" -> Denied == currentAuthorizationStatus
                else -> NotDetermined == currentAuthorizationStatus
            }

        override fun getSerializer(): KSerializer<LocationPermissionsConditionData> =
            serializer()
    }

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Core.Location.Permissions", 1)
    }
}