package com.greencopper.interfacekit.rootview.recipe

import com.greencopper.core.data.writeToPath
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.rootview.RootViewConfiguration
import com.greencopper.interfacekit.rootview.RootViewConfigurationHolder
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileNotFoundException

internal class RootViewRecipeTest {

    private lateinit var rootViewRecipe: RootViewRecipe
    private val featureKey = FeatureKey("feature_key", 98)
    private val featureInfo = FeatureInfo(featureKey)
    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")
    private val rootViewConfigHolder = RootViewConfigurationHolder()

    init {
        Toolkit.setupTest()
    }

    @BeforeEach
    fun setupEach() {
        rootViewRecipe = RootViewRecipe(rootViewConfigHolder)
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
        assertThat(rootViewRecipe.componentPath).isEqualTo("interfaceKit/rootView")
    }

    @Test
    fun tryToProcess_shouldThrow_withFileNotBeingADirectory() {
        val fileNotBeingADirectory = File("test.txt")
        assertThrows<IllegalArgumentException> {
            runTest {
                rootViewRecipe.tryToProcess(fileNotBeingADirectory, fileNotBeingADirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withDirectoryWithNoConfig() {
        val directoryWithNoConfig = File(sourceDirectory, "file_name")
        directoryWithNoConfig.writeText("random_content")
        assertThrows<IllegalArgumentException> {
            runTest {
                rootViewRecipe.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withNonExistentOutPutDirectory() {
        val configFile = File(sourceDirectory, "config.json")
        val rootViewConfiguration = RootViewConfiguration(featureInfo)
        configFile.writeText(rootViewConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                rootViewRecipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        val configFile = File(sourceDirectory, "config.json")
        val rootViewConfiguration = RootViewConfiguration(featureInfo)
        configFile.writeText(rootViewConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                rootViewRecipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_onSuccess_shouldGenerateOutputAtPath() {
        val configFile = File(sourceDirectory, "config.json")
        val rootViewConfiguration = RootViewConfiguration(featureInfo)
        configFile.writeText(rootViewConfiguration.encodeToString())
        assertDoesNotThrow {
            runTest {
                rootViewRecipe.tryToProcess(
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
                rootViewRecipe.tryToApply(File(""))
            }
        }
    }

    @Test
    fun tryToApply_shouldThrow_withWronglyFormattedConfigContent() {
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText("random_content")

        assertThrows<SerializationException> {
            runTest {
                rootViewRecipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfig() {
        val rootViewConfiguration = RootViewConfiguration(featureInfo)
        val configFile = File(sourceDirectory, "config.json")
        rootViewConfiguration.writeToPath(configFile)
        assertDoesNotThrow {
            runTest {
                rootViewRecipe.tryToApply(sourceDirectory)
            }
        }
    }
}
