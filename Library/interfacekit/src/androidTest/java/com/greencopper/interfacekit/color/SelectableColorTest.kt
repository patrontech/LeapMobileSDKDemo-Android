package com.greencopper.interfacekit.color

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.color.repository.ConcreteColorRepository
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SelectableColorTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val colorJson: String =
        context.assets.open("colors/colorsOverride-config.json").bufferedReader()
            .use { it.readText() }

    private lateinit var selectableColor: TestSelectableColor

    @BeforeEach
    internal fun setUp() {
        mockkStatic("com.greencopper.interfacekit.color.UIColorKt")
        Toolkit.setupTest(applicationContext = context)
        val colorRepository = ConcreteColorRepository()
        colorRepository.loadColors(
            App.resolve<Json>().decodeFromString(
                ColorsConfiguration.serializer(),
                colorJson
            )
        )
        bindSingleton<ColorRepository>(colorRepository)
        selectableColor = TestSelectableColor(InterfaceKitColor)
    }

    @AfterEach
    fun afterEach() {
        unmockkStatic("com.greencopper.interfacekit.color.UIColorKt")
    }

    @Test
    fun toColorStateListShouldReturnDefaultColor() {
        every { isDarkMode() } returns false
        val result = selectableColor.toColorStateList()
        assertThat(result.defaultColor).isEqualTo(UIColor.default.background.primary.light)
    }

    class TestSelectableColor(parent: UIColor) : SelectableColor(parent) {
        override val level: String = "test"
        override val normalDefault: Color get() = default.background.primary
        override val selectedDefault: Color get() = default.accent.primary
    }
}
