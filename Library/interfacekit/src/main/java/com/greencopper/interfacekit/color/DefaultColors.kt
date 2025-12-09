package com.greencopper.interfacekit.color

import androidx.core.graphics.toColorInt
import com.greencopper.core.data.KiboSerializable
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import android.graphics.Color as AndroidColor


@Serializable
public data class ColorsConfiguration(val defaultTheme: ColorTheme) :
    KiboSerializable<ColorsConfiguration> {
    override fun getSerializer(): KSerializer<ColorsConfiguration> = serializer()
}

@Serializable
public data class ColorTheme(val default: DefaultColors, val override: JsonObject? = null)

@Serializable
public data class DefaultColors(
    val statusBar: StatusBar,
    val accent: Accent,
    val background: Background,
    val label: Label,
    val fill: Fill,
    val topBar: TopBar,
    val result: Result
) {

    @Serializable
    public data class StatusBar(val light: Style, val dark: Style? = null) {
        @Serializable
        public enum class Style {
            @SerialName("light")
            LIGHT,

            @SerialName("dark")
            DARK
        }
    }

    @Serializable
    public data class Accent(val primary: Color, val secondary: Color)

    @Serializable
    public data class Background(val primary: Color, val secondary: Color)

    @Serializable
    public data class Label(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val quaternary: Color,
        val quinary: Color,
        val senary: Color
    )

    @Serializable
    public data class Fill(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val quaternary: Color,
        val quinary: Color
    )

    @Serializable
    public data class TopBar(val background: Color, val title: Color, val item: Color)

    @Serializable
    public data class Result(val success: Color, val error: Color)
}

@Serializable(with = ColorSerializer::class)
public data class Color(val light: Int, val dark: Int?)
public fun Color.toColorInt(): Int =
    if (isDarkMode()) {
        dark ?: light
    } else {
        light
    }

internal object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        val jsonObject = buildJsonObject {
            put("light", value.light.toColorString())
            value.dark?.let {
                put("dark", it.toColorString())
            }
        }
        encoder.encodeSerializableValue(
            JsonObject.serializer(), jsonObject
        )
    }

    override fun deserialize(decoder: Decoder): Color {
        val jsonObject = decoder.decodeSerializableValue(JsonObject.serializer())
        return Color(
            light = jsonObject["light"]?.jsonPrimitive?.content?.parseToColor()!!,
            dark = jsonObject["dark"]?.jsonPrimitive?.content?.parseToColor()
        )
    }

    private fun Int.toColorString(): String {
        val red = AndroidColor.red(this)
        val green = AndroidColor.green(this)
        val blue = AndroidColor.blue(this)
        val alpha = AndroidColor.alpha(this)

        return "#${red.formatColorPart()}${green.formatColorPart()}${blue.formatColorPart()}${alpha.formatColorPart()}"
    }

    private fun Int.formatColorPart(): String = String.format("%02X", this)
}

internal fun String.parseToColor(): Int {
    var value = this
    val alpha: String

    if (value.length in (8..9)) {
        // Extract alpha from value.
        alpha = value.takeLast(2)
        value = value.dropLast(2)

        value = if (!startsWith("#")) {
            "#$alpha$value"
        } else {
            "#$alpha${value.drop(1)}"
        }

        try {
            return value.toColorInt()
        } catch (throwable: Throwable) {
            App.log.e("Couldn't parse color \"$value\"", throwable = throwable)
        }
    }
    return "#FFFFFFFF".toColorInt()
}
