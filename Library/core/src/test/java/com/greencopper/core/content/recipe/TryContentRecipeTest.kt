package com.greencopper.core.content.recipe

import android.content.Context
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.io.IOException

internal class TryContentRecipeTest {
    val context = mockk<Context>(relaxed = true)
    val classUnderTest = TestContentRecipe()

    init {
        Toolkit.setupTest()
        bindProvider(context)
    }

    @Test
    fun whenOverrideExists_ShouldReturnTrue() {
        every { context.assets.list(any()) } returns arrayOf("test")
        val overrideConfigExists = classUnderTest.overrideConfigExists(context)
        assertThat(overrideConfigExists).isTrue
    }

    @Test
    fun whenOverrideDoesNotExists_ShouldReturnFalse() {
        every { context.assets.list(any()) } throws IOException()
        val overrideConfigExists = classUnderTest.overrideConfigExists(context)
        assertThat(overrideConfigExists).isFalse
    }

    @Test
    fun whenOverrideNull_ShouldReturnFalse() {
        every { context.assets.list(any()) } returns null
        val overrideConfigExists = classUnderTest.overrideConfigExists(context)
        assertThat(overrideConfigExists).isFalse
    }

    @Test
    fun overrideFolder_whenListFilesReturnedNull_DoesntThrow() {
        every { context.cacheDir } returns File("cache")
        every { context.assets.list(classUnderTest.componentPathOverride) } returns null
        assertDoesNotThrow {
            runTest {
                classUnderTest.overrideFolder(context)
            }
        }
    }
}
