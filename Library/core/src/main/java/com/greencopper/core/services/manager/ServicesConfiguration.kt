package com.greencopper.core.services.manager

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.services.iplocation.RestrictedArea
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable;
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE

@Serializable
internal data class ServicesConfiguration(
    val enabledAndroidServices: Map<String, Settings>
): KiboSerializable<ServicesConfiguration> {
    @Serializable(with = ServicesConfigurationStartSerializer::class)
    internal enum class Start {
        AT_LAUNCH,
        IN_RESTRICTED_AREA,
        OUTSIDE_RESTRICTED_AREA,
        BY_USER;

        internal fun startAtLaunch(restrictedArea: RestrictedArea): Boolean =
            when (this) {
                AT_LAUNCH -> true
                IN_RESTRICTED_AREA -> restrictedArea == RestrictedArea.IN_RESTRICTED_AREA
                OUTSIDE_RESTRICTED_AREA -> restrictedArea == RestrictedArea.OUTSIDE_RESTRICTED_AREA
                else -> false
            }
    }

    @Serializable
    internal data class Settings(internal val start: Start)

    override fun getSerializer(): KSerializer<ServicesConfiguration> = serializer()
}

private typealias Start = ServicesConfiguration.Start

internal class ServicesConfigurationStartSerializer: KSerializer<Start> {
    private companion object {
        const val AT_LAUNCH = "atLaunch"
        const val IN_RESTRICTED_AREA = "inRestrictedArea"
        const val OUTSIDE_RESTRICTED_AREA = "outsideRestrictedArea"
        const val BY_USER = "byUser"
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ServicesConfiguration.Start", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Start) =
        encoder.encodeString(
            when (value) {
                ServicesConfiguration.Start.AT_LAUNCH -> AT_LAUNCH
                ServicesConfiguration.Start.IN_RESTRICTED_AREA -> IN_RESTRICTED_AREA
                ServicesConfiguration.Start.OUTSIDE_RESTRICTED_AREA -> OUTSIDE_RESTRICTED_AREA
                ServicesConfiguration.Start.BY_USER -> BY_USER
            }
        )

    override fun deserialize(decoder: Decoder): Start =
        when (val value = decoder.decodeString()) {
            AT_LAUNCH -> ServicesConfiguration.Start.AT_LAUNCH
            IN_RESTRICTED_AREA -> ServicesConfiguration.Start.IN_RESTRICTED_AREA
            OUTSIDE_RESTRICTED_AREA -> ServicesConfiguration.Start.OUTSIDE_RESTRICTED_AREA
            BY_USER -> ServicesConfiguration.Start.BY_USER
            else -> throw SerializationException("Unknown value '$value' for ServicesConfiguration.Start.")
        }
}
