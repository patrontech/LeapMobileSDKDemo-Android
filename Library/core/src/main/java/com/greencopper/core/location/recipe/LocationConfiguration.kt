package com.greencopper.core.location.recipe

import android.location.Location
import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable
internal data class LocationConfiguration (
    val accuracy: Accuracy,
    val frequency: Int,
    val timeout: Int,
    val regions: List<Region>
): KiboSerializable<LocationConfiguration> {

    override fun getSerializer(): KSerializer<LocationConfiguration> = serializer()
}

@Serializable
public enum class Accuracy {
    @SerialName("coarse") COARSE,
    @SerialName("fine") FINE
}

@Serializable
public data class Region(
    val id: Int,
    val name: String,
    @Serializable(with = LocationSerializer::class)
    val location: Location,
    @SerialName("radius")
    val radiusInMeters: Int
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Region) return false
        return other.id == id &&
                other.name == name &&
                other.location.latitude == location.latitude &&
                other.location.longitude == location.longitude &&
                other.radiusInMeters == radiusInMeters
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + location.latitude.hashCode()
        result = 31 * result + location.longitude.hashCode()
        result = 31 * result + radiusInMeters
        return result
    }
}

internal object LocationSerializer : KSerializer<Location> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Location", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Location) {
        val jsonObject = buildJsonObject {
            put("latitude", value.latitude.toString())
            put("longitude", value.longitude.toString())
        }
        encoder.encodeSerializableValue(
            JsonObject.serializer(), jsonObject
        )
    }

    override fun deserialize(decoder: Decoder): Location {
        val jsonObject = decoder.decodeSerializableValue(JsonObject.serializer())
        return Location("").apply {
            latitude = jsonObject["latitude"]?.jsonPrimitive?.content?.toDouble() ?: throw SerializationException("Latitude can't be retrieved")
            longitude = jsonObject["longitude"]?.jsonPrimitive?.content?.toDouble() ?: throw SerializationException("Longitude can't be retrieved")
        }
    }
}
