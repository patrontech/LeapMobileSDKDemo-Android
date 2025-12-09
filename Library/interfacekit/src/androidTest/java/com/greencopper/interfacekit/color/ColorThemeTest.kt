package com.greencopper.interfacekit.color

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test

internal class ColorThemeTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    init {
        Toolkit.setupTest()
    }

    @Test
    fun whenSerializatingText_shouldSerialize() {
        val colorsConfigText =
            context.assets.open("colors/colorsOverride-config.json").bufferedReader().use { it.readText() }
        val themesJson = App.resolve<Json>().parseToJsonElement(colorsConfigText)
        val themes = themesJson.jsonObject.mapValues {
            App.resolve<Json>().decodeFromJsonElement(ColorTheme.serializer(), it.value)
        }
        println(themes)
    }
}