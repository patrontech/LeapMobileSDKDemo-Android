package com.greencopper.interfacekit.textstyle.subsystem

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public data class TextStyleConfiguration(
    val themes: Map<String, Theme>,
) : KiboSerializable<TextStyleConfiguration> {
    override fun getSerializer(): KSerializer<TextStyleConfiguration> = serializer()

    @Serializable
    public data class Theme(
        val default: DefaultSet,
        val override: JsonObject? = null,
    )

    @Serializable
    public data class TextStyle(
        val fonts: List<Font>,
    ) {
        @Serializable
        public data class Font(
            val name: String,
        )
    }

    @Serializable
    public data class DefaultSet(
        val largeTitle: TextStyle,
        val title: Title,
        val headline: Headline,
        val body: Body,
        val caption: Caption,
        val footnote: Footnote,
    )

    @Serializable
    public data class Title(
        val xl: TextStyle,
        val l: TextStyle,
        val m: TextStyle,
        val s: TextStyle,
        val xs: TextStyle,
    )

    @Serializable
    public data class Headline(
        val l: TextStyle,
        val m: TextStyle,
        val s: TextStyle,
    )

    @Serializable
    public data class Body(
        val xl: TextStyle,
        val l: TextStyle,
        val m: TextStyle,
        val s: TextStyle,
        val xs: TextStyle,
    )

    @Serializable
    public data class Caption(
        val l: TextStyle,
        val s: TextStyle,
    )

    @Serializable
    public data class Footnote(
        val m: TextStyle,
        val s: TextStyle,
    )
}
