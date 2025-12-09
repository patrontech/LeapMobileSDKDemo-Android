package com.greencopper.core.asset.recipe

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class AssetsConfiguration(
    val assets: List<Asset> = emptyList(),
    val imagePlaceholderName: String,
    val customImagePlaceholderName: String? = null,
    val failedImageName: String,
    val project: String,
) : KiboSerializable<AssetsConfiguration> {

    override fun getSerializer(): KSerializer<AssetsConfiguration> = serializer()
}

@Serializable
public data class Asset(
    val name: String,
    val url: String,
    val onDemandOnly: Boolean? = false,
    val formats: Map<FormatName, Format>? = emptyMap(),
    val ratio: Float = 1f,
    val priority: Int? = null,
) {
    @Serializable
    public data class Format(
        val origin: Point,
        val size: Size,
    ) {
        @Serializable
        public data class Point(
            val x: Int,
            val y: Int,
        )

        @Serializable
        public data class Size(
            val width: Int,
            val height: Int,
        )

        public enum class Name(public val formatName :String) {
            THUMBNAIL("thumbnail"),
        }
    }
}

internal typealias FormatName = String
