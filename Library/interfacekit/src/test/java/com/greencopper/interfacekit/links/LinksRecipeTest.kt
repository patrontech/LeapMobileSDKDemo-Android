package com.greencopper.interfacekit.links

import com.greencopper.core.data.writeToPath
import com.greencopper.interfacekit.links.recipe.LinksRecipe
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileNotFoundException

internal class LinksRecipeTest {

    private val configHolder = LinksConfigurationHolder()

    private val testLinksConfig = LinksConfiguration(
        mapOf(
            "1" to Route.Present(
                feature = FeatureInfo(key = FeatureKey("", 1))
            ),
            "2" to Route.Push(
                feature = FeatureInfo(key = FeatureKey("", 1))
            )
        ),
        mapOf(
            "3" to FeatureInfo(key = FeatureKey("feature3", 1)),
            "4" to FeatureInfo(key = FeatureKey("feature4", 1))
        )
    )

    private lateinit var linksRecipe: LinksRecipe
    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")

    @BeforeEach
    fun setup() {
        Toolkit.setupTest()
        bindSingleton(configHolder)
        linksRecipe = LinksRecipe(configHolder)
        sourceDirectory.mkdir()
        outputDirectory.mkdir()
    }

    @AfterEach
    fun cleanUp() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun tryToProcess_shouldThrow_withSourceFileNotBeingADirectory() {
        //given
        val fileNotBeingADirectory = File("test.txt")

        //then
        assertThrows<IllegalArgumentException> {
            runTest {
                linksRecipe.tryToProcess(
                    fileNotBeingADirectory,
                    outputDirectory
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withOutputFileNotBeingADirectory() {
        //given
        val fileNotBeingADirectory = File("test.txt")
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText(testLinksConfig.encodeToString())

        //then
        assertThrows<FileNotFoundException> {
            runTest {
                linksRecipe.tryToProcess(
                    sourceDirectory,
                    fileNotBeingADirectory
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withDirectoryWithNoConfig() {
        //given
        val directoryWithNoConfig = File(sourceDirectory, "file_name")
        directoryWithNoConfig.writeText("random_content")

        //then
        assertThrows<IllegalArgumentException> {
            runTest {
                linksRecipe.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        //given
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText(testLinksConfig.encodeToString())

        //then
        assertThrows<FileNotFoundException> {
            runTest {
                linksRecipe.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_onSuccess_shouldGenerateOutputAtPath() {
        //given
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText(testLinksConfig.encodeToString())

        //when
        assertDoesNotThrow {
            runTest {
                linksRecipe.tryToProcess(
                    sourceDirectory,
                    outputDirectory
                )
            }
        }

        //then
        Assertions.assertThat(File(outputDirectory, "config.json").exists()).isTrue
    }

    @Test
    fun tryToApply_shouldThrow_withNonExistingContent() {
        assertThrows<FileNotFoundException> {
            runTest {
                linksRecipe.tryToApply(File(""))
            }
        }
    }

    @Test
    fun tryToApply_shouldThrow_withWronglyFormattedConfigContent() {
        //given
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText("random_content")

        //then
        assertThrows<SerializationException> {
            runTest {
                linksRecipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfig() {
        //given
        val configFile = File(sourceDirectory, "config.json")
        testLinksConfig.writeToPath(configFile)

        //then
        assertDoesNotThrow {
            runTest {
                linksRecipe.tryToApply(sourceDirectory)
            }
        }

        Assertions.assertThat(testLinksConfig).isEqualTo(configHolder.currentConfiguration.value)
    }
}
