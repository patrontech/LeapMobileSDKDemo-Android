package com.greencopper.interfacekit.color

import androidx.core.graphics.toColorInt
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import android.graphics.Color as AndroidColor

internal class DefaultColorsKtTest {

    private val colorTested = "#AABBCCDD".toColorInt()

    @BeforeEach
    fun beforeEach() {
        App = mockk()
        every { App.log.e(any(), throwable = any()) } returns Unit
        mockkStatic("com.greencopper.interfacekit.color.UIColorKt")
    }

    @AfterEach
    fun afterEach() {
        unmockkStatic("com.greencopper.interfacekit.color.UIColorKt")
    }

    @Test
    fun validColor_shouldParse() {
        assertThat("#BBCCDDAA".parseToColor()).isEqualTo(colorTested)
    }

    @Test
    fun noHashColor_shouldParse(){
        assertThat("BBCCDDAA".parseToColor()).isEqualTo(colorTested)
    }

    @Test
    fun noAlpha_shouldFail(){
        assertThat("#BBCCDD".parseToColor()).isNotEqualTo(colorTested)
    }

    @Test
    fun wrongColor_shouldFail() {
        assertThat("#RRGGBBFF".parseToColor()).isNotEqualTo(colorTested)
    }

    @Test
    fun colorToInt_NoDarkMode_ShouldGetLight() {
        every { isDarkMode() } returns false

        val color = Color(AndroidColor.RED, AndroidColor.BLUE)

        assertThat(color.toColorInt()).isEqualTo(color.light)
    }

    @Test
    fun colorToInt_WithDarkMode_ShouldGetDark() {
        every { isDarkMode() } returns true

        val color = Color(AndroidColor.RED, AndroidColor.BLUE)

        assertThat(color.toColorInt()).isEqualTo(color.dark)
    }

    @Test
    fun colorToInt_WithDarkMode_WithoutDarkColor_ShouldGetLight() {
        every { isDarkMode() } returns true

        val color = Color(AndroidColor.RED, null)

        assertThat(color.toColorInt()).isEqualTo(color.light)
    }
}
