package com.greencopper.core.content.recipe

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.recipe.CoreConfigRecipe
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.IOException

internal class RecipeOverrideTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val configHolder = CoreConfigurationHolder()
    private val recipeOverride: TestCoreConfigRecipeOverride

    private val sourceDirectory = File("sourceDirectory")

    init {
        Toolkit.setupTest()
        recipeOverride = TestCoreConfigRecipeOverride(context, configHolder)
    }

    @Test
    fun tryToApply_shouldThrow_withNonExistingContent() {
        assertThrows<IOException> {
            runTest {
                recipeOverride.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_Override() {
        recipeOverride.pathOverride = "testingOverrideContent"
        assertDoesNotThrow {
            runTest {
                recipeOverride.tryToApply(sourceDirectory)
            }
        }

        assertThat(configHolder.currentConfiguration.value?.ota?.apiUrl).isEqualTo("fakeUrl")
    }
}

internal class TestCoreConfigRecipeOverride(
    private val context: Context,
    coreConfigurationHolder: CoreConfigurationHolder,
    var pathOverride: String = "",
) : CoreConfigRecipe(coreConfigurationHolder), RecipeOverride {
    override val componentPathOverride get() = pathOverride
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}
