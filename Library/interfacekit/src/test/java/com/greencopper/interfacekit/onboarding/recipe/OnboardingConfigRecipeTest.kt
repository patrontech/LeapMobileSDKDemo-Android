package com.greencopper.interfacekit.onboarding.recipe

import com.greencopper.core.data.writeToPath
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
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
import java.io.FileNotFoundException

internal class OnboardingConfigRecipeTest {

    private val configHolder = OnboardingConfigurationHolder()
    private val recipe = OnboardingConfigRecipe(configHolder)

    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")

    init {
        Toolkit.setupTest()
    }

    @BeforeEach
    fun beforeEach() {
        configHolder.currentConfiguration.value = null
        sourceDirectory.mkdir()
        outputDirectory.mkdir()
    }

    @AfterEach
    fun afterEach() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun tryToProcess_shouldThrow_withFileNotBeingADirectory() {
        val fileNotBeingADirectory = File("test.txt")
        assertThrows<IllegalArgumentException> {
            runTest {
                recipe.tryToProcess(fileNotBeingADirectory, fileNotBeingADirectory)
            }
        }
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
    fun tryToProcess_shouldThrow_withNonExistentOutPutDirectory() {
        val configFile = File(sourceDirectory, "config.json")
        val config = OnboardingConfiguration(emptyList())
        configFile.writeText(config.encodeToString())

        assertThrows<FileNotFoundException> {
            runTest {
                recipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        val configFile = File(sourceDirectory, "config.json")
        val config = OnboardingConfiguration(emptyList())
        configFile.writeText(config.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                recipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_onSuccess_shouldGenerateOutputAtPath() {
        val configFile = File(sourceDirectory, "config.json")
        val config = OnboardingConfiguration(emptyList())
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
    fun tryToApply_shouldThrow_withNonExistingContent() {
        assertThrows<FileNotFoundException> {
            runTest {
                recipe.tryToApply(File(""))
            }
        }
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
        val config = OnboardingConfiguration(listOf(
            OnboardingPageInfo("id", OnboardingPageKey("name", 2))
        ))

        val configFile = File(sourceDirectory, "config.json")
        config.writeToPath(configFile)

        assertDoesNotThrow {
            runTest {
                recipe.tryToApply(sourceDirectory)
            }
        }
    }
}
