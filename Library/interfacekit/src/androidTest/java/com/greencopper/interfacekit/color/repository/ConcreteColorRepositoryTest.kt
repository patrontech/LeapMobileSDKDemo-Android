package com.greencopper.interfacekit.color.repository

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.color.*
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import android.graphics.Color as AndroidColor

internal class ConcreteColorRepositoryTest : CoroutineTest() {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val colorJson: String =
        context.assets.open("colors/colorsOverride-config.json").bufferedReader()
            .use { it.readText() }

    init {
        mockkStatic("com.greencopper.interfacekit.color.UIColorKt")
        Toolkit.setupTest(applicationContext = context)
        bindSingleton<ColorRepository>(ConcreteColorRepository())
    }

    override fun afterEach() {
        unmockkStatic("com.greencopper.interfacekit.color.UIColorKt")
    }

    @Test
    fun whenLoadingColors_shouldHaveDefaultColors() {
        assertDoesNotThrow {
            val colorRepository = createColorRepository()
            colorRepository.getDefaultColors()
        }
    }

    @Test
    fun whenGettingColorInt_whenLight_shouldGetLightColor() {
        every { isDarkMode() } returns false

        val colorValue = Color(AndroidColor.WHITE, AndroidColor.BLACK).toColorInt()
        assertThat(colorValue).isEqualTo(AndroidColor.WHITE)
    }

    @Test
    fun whenGettingColorInt_whenDark_shouldGetDarkColor() {
        every { isDarkMode() } returns true

        val colorValue = Color(AndroidColor.WHITE, AndroidColor.BLACK).toColorInt()
        assertThat(colorValue).isEqualTo(AndroidColor.BLACK)
    }

    @Test
    fun whenGettingOverride_whenDark_whenDarkUnavailable_shouldGetLight() {
        every { isDarkMode() } returns true

        val colorRepository = createColorRepository()

        val colorValue =
            colorRepository.getOverrideColorInt(InterfaceKitColor.bottomBar.getLevels("background"))
        assertThat(colorValue).isNotNull
    }

    @Test
    fun whenGettingOverride_whenDark_whenDarkAvailable_shouldGetDark() {
        every { isDarkMode() } returns true
        val colorRepository = createColorRepository()

        val colorValue =
            colorRepository.getOverrideColorInt(InterfaceKitColor.bottomBar.item.getLevels("selected"))
        assertThat(colorValue).isNotNull
    }

    @Test
    fun whenGettingOverride_whenLight_whenLightUnAvailable_shouldGetNull() {
        every { isDarkMode() } returns false
        val colorRepository = createColorRepository()

        val colorValue =
            colorRepository.getOverrideColorInt(InterfaceKitColor.bottomBar.item.getLevels("selected"))
        assertThat(colorValue).isNull()
    }

    @Test
    fun whenGettingOverride_whenLight_whenLightAvailable_shouldGetLight() {
        every { isDarkMode() } returns false
        val colorRepository = createColorRepository()

        val colorValue =
            colorRepository.getOverrideColorInt(InterfaceKitColor.bottomBar.getLevels("background"))
        assertThat(colorValue).isNotNull
    }

    @Test
    fun whenGettingOverride_withNonExistantColor_shouldGetNull() {
        val colorRepository = createColorRepository()

        val colorValue = colorRepository.getOverrideColorInt(listOf("someString"))
        assertThat(colorValue).isNull()
    }

    private fun createColorRepository(): ConcreteColorRepository =
        ConcreteColorRepository().apply {
            loadColors(KiboSerializable.decodeFromString(colorJson))
        }
}
