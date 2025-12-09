package com.greencopper.thuzi.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

/**
 * Represents a `DeviceSession` for use by Thuzi.
 *
 * A `DeviceSession` is the combination of the current `installationId` with a random UUID
 * representing a session. This session id has to exist even if the user is not logged in, and
 * it will remain tied to the same user after they login.
 *
 * This means that we generate a new value when accessed (unless we already have one) and whenever
 * the user _logs out_, but **not** when the user logs in. If we generate one when the user logs in,
 * we'll be invalidating the one we just sent to Thuzi in order to register the user.
 */
@Serializable(with = DeviceSessionSerializer::class)
public data class DeviceSession(
    public val urn: String
) {

    init {
        val pattern = Regex("^urn:device-session:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$")
        require(pattern.matches(urn)) {
            "The string '$urn' is invalid as a DeviceSession URN."
        }
    }

    public constructor(installationId: String, sessionId: UUID = UUID.randomUUID()):
        this("urn:device-session:$installationId:$sessionId")

    override fun toString(): String = urn
}

internal object DeviceSessionSerializer: KSerializer<DeviceSession> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DeviceSession", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DeviceSession) {
        encoder.encodeString(value.urn)
    }

    override fun deserialize(decoder: Decoder): DeviceSession =
        DeviceSession(decoder.decodeString())
}

