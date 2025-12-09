package com.greencopper.interfacekit.editorial.recipe

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import com.greencopper.interfacekit.mocks.MockEditorialPageRepository
import java.io.File

internal class EditorialPageRecipeTest {
    private var mockPath: String? = null
    private val mockRepository = MockEditorialPageRepository(
        setContentDirectoryPath = { path -> mockPath = path }
    )

    private val recipe = EditorialPageRecipe(mockRepository)

    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")

    @BeforeEach
    fun beforeEach() {
        mockPath = null
        sourceDirectory.mkdir()
        outputDirectory.mkdir()
    }

    @AfterEach
    fun afterEach() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun tryToProcess_withNonDirectorySource_shouldThrow() {
        val notDirectory = File("test.txt")

        assertThrows<IllegalArgumentException> {
            runTest {
                recipe.tryToProcess(
                    notDirectory,
                    outputDirectory
                )
            }
        }
    }

    @Test
    fun tryToProcess_withDirectories_shouldNotThrow() {
        runTest {
            recipe.tryToProcess(
                sourceDirectory,
                outputDirectory
            )
        }
    }

    @Test
    fun tryToApply_setsPath() {
        runTest {
            recipe.tryToApply(File("test"))
            Assertions.assertThat(mockPath).isNotNull
        }
    }
}
