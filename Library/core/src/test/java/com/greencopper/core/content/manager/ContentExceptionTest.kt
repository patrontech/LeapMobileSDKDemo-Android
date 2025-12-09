package com.greencopper.core.content.manager

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.TestContentRecipe
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

internal class ContentExceptionTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun processorRunExceptionMessage() {
        val archive = ContentArchive(File("somePath"), "secretText")
        val content = Content(archive, 1, 1, "defaultTag", OTAContent.Type.Release)

        val illegalArgumentException = IllegalArgumentException()
        var exception =
            ContentException.ProcessorProcessException(illegalArgumentException, content)
        assertThat(exception).hasMessageContaining("$content")
        assertThat(exception).hasMessageContaining("$illegalArgumentException")

        val contentException = ContentException.RecipeException(
            illegalArgumentException,
            TestContentRecipe()
        )
        exception = ContentException.ProcessorProcessException(contentException, content)
        assertThat(exception).hasMessageContaining("$content")
        assertThat(exception).hasMessageContaining("$contentException")
        assertThat(exception).hasMessageContaining("$illegalArgumentException")

        exception = ContentException.ProcessorProcessException(null, content)
        assertThat(exception).hasMessageContaining("$content")
        assertThat(exception).hasMessageContaining("null")
    }

    @Test
    fun recipeExceptionMessage() {
        val recipe = TestContentRecipe()
        val exception = ContentException.RecipeException(IllegalArgumentException(), recipe)
        assertThat(exception).hasMessageContaining("$recipe")
    }

    @Test
    fun couldntOpenContentExceptionMessage() {
        val thrown = IllegalArgumentException()
        val exception = ContentException.CouldntOpenContentException(thrown)
        assertThat(exception).hasMessageContaining("$thrown")
    }

    @Test
    fun processorApplyExceptionMessage() {
        runTest {
            val storageManager: StorageManager = App.resolve()
            val contentConfiguration = RunConfiguration.build(storageManager, App.resolve()).content
            val archive = ContentArchive(
                storageManager.getAssetAsFile("content/${contentConfiguration.fileName}"),
                contentConfiguration.secret
            )
            val content = Content(
                archive,
                contentConfiguration.version,
                contentConfiguration.schema,
                "defaultTag",
                OTAContent.Type.Release,
            )

            val thrown = IllegalArgumentException()
            val exception = ContentException.ProcessorApplyException(thrown, content)
            assertThat(exception).hasMessageContaining("$thrown")
            assertThat(exception).hasMessageContaining("$content")
        }
    }

    @Test
    fun unreadyStateExceptionMessage() {
        val state = State.ProcessingFailed()
        val exception = ContentException.UnreadyStateException(state)
        assertThat(exception).hasMessageContaining("$state")
    }

    @Test
    fun recipesNotMatchingExceptionMessage() {
        val actual = setOf<ContentRecipeKey>()
        val expected = setOf(
            ContentRecipeKey("OTARecipe", 5, 1),
            ContentRecipeKey("TestRecipe", 4, 1)
        )

        val exception = ContentException.RecipesNotMatchingException(actual, expected)
        assertThat(exception).hasMessageContaining("$actual")
        assertThat(exception).hasMessageContaining("$expected")
    }

    @Test
    fun schemaNotMatchingExceptionMessage() {
        val actual = 2
        val expected = 5
        val exception = ContentException.SchemaNotMatchingException(actual, expected)
        assertThat(exception).hasMessageContaining("$actual")
        assertThat(exception).hasMessageContaining("$expected")
    }
}
