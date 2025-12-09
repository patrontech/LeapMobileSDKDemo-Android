package com.greencopper.thuzi.account.registration.recipe

import com.greencopper.core.data.writeToPath
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.account.registration.model.RegistrationConfiguration
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileNotFoundException

internal class RegistrationRecipeTest {

    private lateinit var registrationRecipe: RegistrationRecipe
    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")
    private val registrationConfigHolder = RegistrationConfigurationHolder()

    @BeforeEach
    fun setup() {
        Toolkit.setupTest()
        registrationRecipe = RegistrationRecipe(registrationConfigHolder)
        sourceDirectory.mkdir()
        outputDirectory.mkdir()
    }

    @AfterEach
    fun cleanUp() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun getComponentPath() {
        Assertions.assertThat(registrationRecipe.componentPath).isEqualTo("thuzi/registration/")
    }

    @Test
    fun tryToProcess_shouldThrow_withFileNotBeingADirectory() {
        val fileNotBeingADirectory = File("test.txt")
        assertThrows<IllegalArgumentException> {
            runTest {
                registrationRecipe.tryToProcess(fileNotBeingADirectory, fileNotBeingADirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withDirectoryWithNoConfig() {
        val directoryWithNoConfig = File(sourceDirectory, "file_name")
        directoryWithNoConfig.writeText("random_content")
        assertThrows<IllegalArgumentException> {
            runTest {
                registrationRecipe.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withNonExistentOutPutDirectory() {
        val configFile = File(sourceDirectory, "config.json")
        val registrationConfiguration = RegistrationConfiguration(
            "api_url",
            "activation_url",
            "device_linking_url",
            "user_state_update_url",
            "brand_id",
            "event_id",
            "project",
            ScreenNameAnalytics("test"),
            "accountDeletionApiUrl",
        )
        configFile.writeText(registrationConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                registrationRecipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        val configFile = File(sourceDirectory, "config.json")
        val registrationConfiguration = RegistrationConfiguration(
            "api_url",
            "activation_url",
            "device_linking_url",
            "user_state_update_url",
            "brand_id",
            "event_id",
            "project",
            ScreenNameAnalytics("test"),
           "accountDeletionApiUrl",
        )
        configFile.writeText(registrationConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                registrationRecipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_onSuccess_shouldGenerateOutputAtPath() {
        val configFile = File(sourceDirectory, "config.json")
        val registrationConfiguration = RegistrationConfiguration(
            "api_url",
            "activation_url",
            "device_linking_url",
            "user_state_update_url",
            "brand_id",
            "event_id",
            "project",
            ScreenNameAnalytics("test"),
            "accountDeletionApiUrl",
        )
        configFile.writeText(registrationConfiguration.encodeToString())
        org.junit.jupiter.api.assertDoesNotThrow {
            runTest {
                registrationRecipe.tryToProcess(
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
                registrationRecipe.tryToApply(File(""))
            }
        }
    }

    @Test
    fun tryToApply_shouldThrow_withWronglyFormattedConfigContent() {
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText("random_content")

        assertThrows<SerializationException> {
            runTest {
                registrationRecipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfig() {
        val registrationConfiguration = RegistrationConfiguration(
            "api_url",
            "activation_url",
            "device_linking_url",
            "user_state_update_url",
            "brand_id",
            "event_id",
            "project",
            ScreenNameAnalytics("test"),
            "accountDeletionApiUrl",
        )
        val configFile = File(sourceDirectory, "config.json")
        registrationConfiguration.writeToPath(configFile)
        assertDoesNotThrow {
            runTest {
                registrationRecipe.tryToApply(sourceDirectory)
            }
        }
        assert(registrationConfigHolder.currentConfiguration.value == registrationConfiguration)
        registrationConfigHolder.currentConfiguration.value = null
    }
}
