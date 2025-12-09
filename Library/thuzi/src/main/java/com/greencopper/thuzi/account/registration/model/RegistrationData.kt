package com.greencopper.thuzi.account.registration.model

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class RegistrationResponse(
    val type: String,
    /**
     * The payload of the registration response.
     *
     * The value of [data] will vary depending upon the value of [type], so
     * it cannot be hard-coded to a particular Kotlin type.
     */
    val data: JsonElement
) : KiboSerializable<RegistrationResponse> {
    public companion object {
        public const val ACTIVATION_COMPLETE: String = "activationComplete"
        public const val DEVICE_LINKING_COMPLETE: String = "deviceLinkingComplete"
        public const val ATTENDEE_AUTHENTICATED: String = "attendeeAuthenticated"
        public const val ACTIVATION_RESTARTED: String = "activationRestarted"
        public const val DEVICE_LINKING_RESTARTED: String = "deviceLinkingRestarted"
    }

    override fun getSerializer(): KSerializer<RegistrationResponse> = serializer()
}

@Serializable
public data class RegistrationData(
    val qrCode: String?,
    val attendeeId: String?,
    val authToken: String,
    val authTokenExpiresOn: String, // "yyyy-MM-dd'T'HH:mm:ss'Z'"
    val attendee: Attendee? = null
) : KiboSerializable<RegistrationData> {
    override fun getSerializer(): KSerializer<RegistrationData> = serializer()

    @Serializable
    public data class Attendee(
        val firstName: String? = null
    ) : KiboSerializable<Attendee> {
        override fun getSerializer(): KSerializer<Attendee> = serializer()
    }
}
