package com.greencopper.interfacekit.ui.views

import android.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class DotsIndicatorViewTest {
    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    @DisplayName("When arguments are valid, When calling setup, Then it should succeed")
    fun setupShouldSucceed() {
        val classUnderTest = DotsIndicatorView(context)
        assertDoesNotThrow {
            classUnderTest.setup(
                numberOfDots = 3,
                currentPosition = 0,
                selectedDotColor = Color.GREEN,
                defaultDotColor = Color.MAGENTA
            )
        }
    }

    @Test
    @DisplayName("When arguments are invalid, When calling setup, Then it should fail")
    fun setupShouldFail() {
        val classUnderTest = DotsIndicatorView(context)
        assertThrows<IllegalArgumentException> {
            classUnderTest.setup(
                numberOfDots = 3,
                currentPosition = 4,
                selectedDotColor = Color.GREEN,
                defaultDotColor = Color.MAGENTA
            )
        }
    }

    @Test
    @DisplayName("When arguments are valid, When calling setup, Then the right number of child views should be created")
    fun setupShouldCreateTheRightAmountOfViews() {
        val classUnderTest = DotsIndicatorView(context)
        classUnderTest.setup(
            numberOfDots = 3,
            currentPosition = 0,
            selectedDotColor = Color.GREEN,
            defaultDotColor = Color.MAGENTA
        )
        assertThat(classUnderTest.childCount).isEqualTo(3)
    }
}