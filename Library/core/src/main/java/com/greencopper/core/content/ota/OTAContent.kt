package com.greencopper.core.content.ota

import com.greencopper.core.content.serializers.ZonedDateTimeWithInstantSerializer
import com.greencopper.core.data.KiboSerializable
import com.greencopper.toolkit.extensions.toZonedDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
/** API Representation of a content version */
public data class OTAContent(
    val url: String? = null,
    val project: String,
    @SerialName("date") private val dateString: String? = null,
    val version: Int,
    @SerialName("type") private val typeString: String,
    val schema: Int,
): KiboSerializable<OTAContent> {
    override fun getSerializer(): KSerializer<OTAContent> = serializer()

    @Serializable(with = ZonedDateTimeWithInstantSerializer::class)
    val creationDate: ZonedDateTime? = dateString.toZonedDateTime()
    val versionType: Type? = when (typeString) {
        "release" -> Type.Release
        "draft" -> Type.Draft
        "in_progress" -> Type.InProgress
        else -> null
    }

    public enum class Type {
        Release,
        Draft,
        InProgress
    }
}
