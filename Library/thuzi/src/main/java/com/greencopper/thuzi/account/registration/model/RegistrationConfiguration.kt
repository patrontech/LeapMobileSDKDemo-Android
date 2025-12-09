package com.greencopper.thuzi.account.registration.model

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(with = RegistrationConfigurationSerializer::class)
public data class RegistrationConfiguration(
    val apiUrl: String,
    val activationUrl: String,
    val deviceLinkingUrl: String?,
    val userStateUpdateUrl: String,
    val brandId: String,
    val eventId: String,
    val project: String,
    val analytics: ScreenNameAnalytics,
    val accountDeletionApiUrl: String,
): KiboSerializable<RegistrationConfiguration> {

    override fun getSerializer(): KSerializer<RegistrationConfiguration> = serializer()
}

@OptIn(ExperimentalSerializationApi::class)
internal object RegistrationConfigurationSerializer: KSerializer<RegistrationConfiguration> {
    private const val API_URL = 0
    private const val ACTIVATION_URL = 1
    private const val ACTIVATION_SESSION_URL = 2
    private const val DEVICE_LINKING_URL = 3
    private const val DEVICE_LINKING_SESSION_URL = 4
    private const val USERSTATE_UPDATE_URL = 5
    private const val BRAND_ID = 6
    private const val EVENT_ID = 7
    private const val PROJECT = 8
    private const val ANALYTICS = 9
    private const val ACCOUNT_DELETION_API_URL = 10

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("RegistrationConfiguration") {
        element<String>("apiUrl")
        element<String>("activationUrl", isOptional = true)
        element<String>("activationSessionUrl", isOptional = true)
        element<String>("deviceLinkingUrl", isOptional = true)
        element<String>("deviceLinkingSessionUrl", isOptional = true)
        element<String>("userStateUpdateUrl")
        element<String>("brandId")
        element<String>("eventId")
        element<String>("project")
        element<ScreenNameAnalytics>("analytics")
        element<String>("accountDeletionApiUrl")
    }

    override fun deserialize(decoder: Decoder): RegistrationConfiguration =
        decoder.decodeStructure(descriptor) {
            lateinit var apiUrl: String
            var activationUrl: String? = null
            var activationSessionUrl: String? = null
            var deviceLinkingUrl: String? = null
            var deviceLinkingSessionUrl: String? = null
            lateinit var userStateUpdateUrl: String
            lateinit var brandId: String
            lateinit var eventId: String
            lateinit var project: String
            lateinit var analytics: ScreenNameAnalytics
            lateinit var accountDeletionApiUrl: String

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    API_URL -> apiUrl = decodeStringElement(descriptor, index)
                    ACTIVATION_URL -> activationUrl = decodeNullableSerializableElement(descriptor, index, String.serializer())
                    ACTIVATION_SESSION_URL -> activationSessionUrl = decodeNullableSerializableElement(descriptor, index, String.serializer())
                    DEVICE_LINKING_URL -> deviceLinkingUrl = decodeNullableSerializableElement(descriptor, index, String.serializer())
                    DEVICE_LINKING_SESSION_URL -> deviceLinkingSessionUrl = decodeNullableSerializableElement(descriptor, index, String.serializer())
                    USERSTATE_UPDATE_URL -> userStateUpdateUrl = decodeStringElement(descriptor, index)
                    BRAND_ID -> brandId = decodeStringElement(descriptor, index)
                    EVENT_ID -> eventId = decodeStringElement(descriptor, index)
                    PROJECT -> project = decodeStringElement(descriptor, index)
                    ANALYTICS -> analytics = decodeSerializableElement(descriptor, index, ScreenNameAnalytics.serializer())
                    ACCOUNT_DELETION_API_URL -> accountDeletionApiUrl = decodeStringElement(descriptor, index)
                    else -> break
                }
            }
            RegistrationConfiguration(
                apiUrl = apiUrl,
                activationUrl = activationSessionUrl ?: activationUrl!!,
                deviceLinkingUrl = deviceLinkingSessionUrl ?: deviceLinkingUrl,
                userStateUpdateUrl = userStateUpdateUrl,
                brandId = brandId,
                eventId = eventId,
                project = project,
                analytics = analytics,
                accountDeletionApiUrl = accountDeletionApiUrl,
            )
        }
    override fun serialize(encoder: Encoder, value: RegistrationConfiguration) = encoder.encodeStructure(descriptor) {
        encodeStringElement(descriptor, API_URL, value.apiUrl)
        encodeNullableSerializableElement(descriptor, ACTIVATION_SESSION_URL, String.serializer(), value.activationUrl)
        encodeNullableSerializableElement(descriptor, DEVICE_LINKING_SESSION_URL, String.serializer(), value.deviceLinkingUrl)
        encodeStringElement(descriptor, USERSTATE_UPDATE_URL, value.userStateUpdateUrl)
        encodeStringElement(descriptor, BRAND_ID, value.brandId)
        encodeStringElement(descriptor, EVENT_ID, value.eventId)
        encodeStringElement(descriptor, PROJECT, value.project)
        encodeSerializableElement(descriptor, ANALYTICS, ScreenNameAnalytics.serializer(), value.analytics)
        encodeStringElement(descriptor, ACCOUNT_DELETION_API_URL, value.accountDeletionApiUrl)
    }

}
