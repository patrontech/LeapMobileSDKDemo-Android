package com.greencopper.interfacekit.interests.recipe

import com.greencopper.core.data.writeToPath
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class InterestsRecipeTests {
    
    private val configHolder = InterestsConfigurationHolder()
    private val recipe = InterestsRecipe(configHolder)

    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")
    private val config = InterestsConfiguration(listOf(
        Interest("1", "name", 1, "analyticsName1", listOf("tag1", "tag2")),
        Interest("2", "name2", 2, "analyticsName2"),
    ))

    init {
        Toolkit.setupTest()
    }

    @BeforeEach
    fun beforeEach() {
        sourceDirectory.mkdir()
        outputDirectory.mkdir()
    }

    @AfterEach
    fun afterEach() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun tryToProcess_shouldThrow_withDirectoryWithNoConfig() {
        val directoryWithNoConfig = File(sourceDirectory, "file_name")
        directoryWithNoConfig.writeText("random_content")
        assertThrows<IllegalArgumentException> {
            runTest {
                recipe.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_onSuccess_shouldGenerateOutputAtPath() {
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText(config.encodeToString())
        assertDoesNotThrow {
            runTest {
                recipe.tryToProcess(
                    sourceDirectory,
                    outputDirectory
                )
            }
        }
        assert(File(outputDirectory, "config.json").exists())
    }

    @Test
    fun tryToApply_shouldThrow_withWronglyFormattedConfigContent() {
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText("random_content")

        assertThrows<SerializationException> {
            runTest {
                recipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfig() {
        val configFile = File(sourceDirectory, "config.json")
        config.writeToPath(configFile)

        assertDoesNotThrow {
            runTest {
                recipe.tryToApply(sourceDirectory)
            }
        }
    }
}
