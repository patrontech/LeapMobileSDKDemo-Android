package com.greencopper.interfacekit.widgets.recipe

import com.greencopper.core.data.writeToPath
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.WidgetCollectionConfigurationHolder
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileNotFoundException

internal class WidgetCollectionRecipeTest {

    private lateinit var widgetCollectionRecipe: WidgetCollectionRecipe
    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")
    private val widgetInfo = WidgetCollectionConfiguration.Instance.WidgetInfo(
        WidgetCollectionConfiguration.Instance.WidgetKey("key", 99),
        JsonPrimitive(""),
        null
    )
    private val widgetCollectionConfigHolder = WidgetCollectionConfigurationHolder()
    private val widgetInstance =
        WidgetCollectionConfiguration.Instance(widgets = listOf(widgetInfo))

    init {
        Toolkit.setupTest()
    }

    @BeforeEach
    fun setup() {
        widgetCollectionRecipe = WidgetCollectionRecipe(widgetCollectionConfigHolder)
        sourceDirectory.mkdir()
        outputDirectory.mkdir()
    }

    @AfterEach
    fun cleanUp() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun componentPath() {
        Assertions.assertThat(widgetCollectionRecipe.componentPath).isEqualTo("interfaceKit/widgetCollection")
    }

    @Test
    fun tryToProcess_shouldThrow_withFileNotBeingADirectory() {
        val fileNotBeingADirectory = File("test.txt")
        assertThrows<IllegalArgumentException> {
            runTest {
                widgetCollectionRecipe.tryToProcess(fileNotBeingADirectory, fileNotBeingADirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withDirectoryWithNoConfig() {
        val directoryWithNoConfig = File(sourceDirectory, "file_name")
        directoryWithNoConfig.writeText("random_content")
        assertThrows<IllegalArgumentException> {
            runTest {
                widgetCollectionRecipe.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withNonExistentOutPutDirectory() {
        val configFile = File(sourceDirectory, "config.json")
        val widgetCollectionConfiguration = WidgetCollectionConfiguration(
            mapOf("test" to widgetInstance)
        )
        configFile.writeText(widgetCollectionConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                widgetCollectionRecipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        val configFile = File(sourceDirectory, "config.json")
        val widgetCollectionConfiguration = WidgetCollectionConfiguration(
            mapOf("test" to widgetInstance)
        )
        configFile.writeText(widgetCollectionConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                widgetCollectionRecipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_onSuccess_shouldGenerateOutputAtPath() {
        val configFile = File(sourceDirectory, "config.json")
        val widgetCollectionConfiguration = WidgetCollectionConfiguration(
            mapOf("test" to widgetInstance)
        )
        configFile.writeText(widgetCollectionConfiguration.encodeToString())
        assertDoesNotThrow {
            runTest {
                widgetCollectionRecipe.tryToProcess(
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
                widgetCollectionRecipe.tryToApply(File(""))
            }
        }
    }

    @Test
    fun tryToApply_shouldThrow_withWronglyFormattedConfigContent() {
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText("random_content")

        assertThrows<SerializationException> {
            runTest {
                widgetCollectionRecipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfig() {
        val widgetCollectionConfiguration = WidgetCollectionConfiguration(
            mapOf("test" to widgetInstance)
        )
        val configFile = File(sourceDirectory, "config.json")
        widgetCollectionConfiguration.writeToPath(configFile)
        assertDoesNotThrow {
            runTest {
                widgetCollectionRecipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun resetConfig_shouldClear() {
        widgetCollectionConfigHolder.currentConfiguration.value = WidgetCollectionConfiguration(
            mapOf("test" to widgetInstance)
        )
        widgetCollectionRecipe.reset()

        assertThat(widgetCollectionConfigHolder.currentConfiguration.value).isNull()
    }
}
