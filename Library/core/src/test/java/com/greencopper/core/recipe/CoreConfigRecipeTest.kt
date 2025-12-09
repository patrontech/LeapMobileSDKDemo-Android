package com.greencopper.core.recipe

import android.content.Context
import com.greencopper.core.data.writeToPath
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileNotFoundException

internal class CoreConfigRecipeTest {

    private val context: Context = mockk(relaxed = true)
    private lateinit var coreConfigRecipe: CoreConfigRecipe
    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")
    private val coreConfigHolder = CoreConfigurationHolder()

    init {
        Toolkit.setupTest(applicationContext = context)
    }

    @BeforeEach
    fun setup() {
        coreConfigRecipe = CoreConfigRecipe(coreConfigHolder)
        sourceDirectory.mkdir()
        outputDirectory.mkdir()
    }

    @AfterEach
    fun cleanUp() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun tryToProcess_shouldThrow_withFileNotBeingADirectory() {
        val fileNotBeingADirectory = File("test.txt")
        assertThrows<IllegalArgumentException> {
            runTest {
                coreConfigRecipe.tryToProcess(fileNotBeingADirectory, fileNotBeingADirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withDirectoryWithNoConfig() {
        val directoryWithNoConfig = File(sourceDirectory, "file_name")
        directoryWithNoConfig.writeText("random_content")
        assertThrows<IllegalArgumentException> {
            runTest {
                coreConfigRecipe.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withNonExistentOutPutDirectory() {
        val configFile = File(sourceDirectory, "config.json")
        val coreConfiguration = CoreConfiguration(
            CoreConfiguration.RemoteState("", 1),
            null,
            CoreConfiguration.OTA(""),
            null,
            CoreConfiguration.ContentConfig(60, listOf())
        )
        configFile.writeText(coreConfiguration.encodeToString())

        assertThrows<FileNotFoundException> {
            runTest {
                coreConfigRecipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        val configFile = File(sourceDirectory, "config.json")
        val coreConfiguration = CoreConfiguration(
            CoreConfiguration.RemoteState("", 1),
            null,
            CoreConfiguration.OTA(""),
            null,
            CoreConfiguration.ContentConfig(60, listOf())
        )
        configFile.writeText(coreConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                coreConfigRecipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_onSuccess_shouldGenerateOutputAtPath() {
        val configFile = File(sourceDirectory, "config.json")
        val coreConfiguration = CoreConfiguration(
            CoreConfiguration.RemoteState("api_url", 1),
            CoreConfiguration.Notifications("api_url"),
            CoreConfiguration.OTA("api_url"),
            null,
            CoreConfiguration.ContentConfig(60, emptyList()),
            mapOf("key" to "value")
        )
        configFile.writeText(coreConfiguration.encodeToString())
        assertDoesNotThrow {
            runTest {
                coreConfigRecipe.tryToProcess(
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
                coreConfigRecipe.tryToApply(File(""))
            }
        }
    }

    @Test
    fun tryToApply_shouldThrow_withWronglyFormattedConfigContent() {
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText("random_content")

        assertThrows<SerializationException> {
            runTest {
                coreConfigRecipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfig() {
        val coreConfiguration = CoreConfiguration(
            CoreConfiguration.RemoteState("api_url", 1),
            CoreConfiguration.Notifications("api_url"),
            CoreConfiguration.OTA("api_url"),
            null,
            CoreConfiguration.ContentConfig(60, emptyList()),
            mapOf("1" to "1", "2" to "2")
        )

        val configFile = File(sourceDirectory, "config.json")
        coreConfiguration.writeToPath(configFile)

        assertDoesNotThrow {
            runTest {
                coreConfigRecipe.tryToApply(sourceDirectory)
            }
        }
    }
}
