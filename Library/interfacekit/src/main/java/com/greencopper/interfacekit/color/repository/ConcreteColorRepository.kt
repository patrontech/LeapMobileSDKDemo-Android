package com.greencopper.interfacekit.color.repository

import com.greencopper.interfacekit.color.*
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.w
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal class ConcreteColorRepository : ColorRepository {

    private lateinit var theme: ColorTheme

    override fun loadColors(configuration: ColorsConfiguration) {
        theme = configuration.defaultTheme
    }

    private fun getJsonData(levels: List<String>): JsonObject? {
        var colorLevel: JsonObject = theme.override ?: return null
        levels.forEach {
            val levelValue = colorLevel[it] ?: return null
            colorLevel = levelValue.jsonObject
        }
        return colorLevel
    }

    override fun getOverrideColorInt(levels: List<String>): Int? {
        val colorLevel = getJsonData(levels) ?: return null
        val color = extractOverrideColor(colorLevel) ?: return null
        return color.toColorInt()
    }

    override fun getOverrideStatusBarColor(levels: List<String>): OverrideStatusBar? {
        val colorLevel = getJsonData(levels) ?: return null
        return extractColorStyle(colorLevel)
    }

    private fun extractColorStyle(jsonData: JsonObject): OverrideStatusBar? =
        try {
            // Use default Json to NOT ignore unknown keys
            Json.decodeFromJsonElement(OverrideStatusBar.serializer(), jsonData)
        } catch (t: Throwable) {
            App.log.w("Couldn't extract color style from JsonElement $jsonData")
            null
        }

    override fun getDefaultColors(): DefaultColors = theme.default

    private fun extractOverrideColor(levelValue: JsonElement): OverrideColor? =
        try {
            // Use default Json to NOT ignore unknown keys
            Json.decodeFromJsonElement(OverrideColor.serializer(), levelValue)
        } catch (t: Throwable) {
            App.log.w("Couldn't extract color from JsonElement $levelValue")
            null
        }
}
