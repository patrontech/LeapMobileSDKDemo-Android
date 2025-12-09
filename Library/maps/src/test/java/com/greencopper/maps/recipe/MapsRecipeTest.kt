package com.greencopper.maps.recipe

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.data.writeToPath
import com.greencopper.maps.common.LocationData
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileNotFoundException

internal class MapsRecipeTest {

    private val localizationService = MockLocalizationService()
    private val configHolder = ConcreteMapsRepository(localizationService)

    private val mapsConfig = MapsConfiguration(
        mapOf(
            "1" to LocationDetailConfigurationData(
                name = "name",
                subtitle = "subtitle",
                address = "address",
                images = listOf(),
                description = "description",
                bottomWidgetCollection = null
            )
        )
    )

    private lateinit var mapsRecipe: MapsRecipe
    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")

    init {
        Toolkit.setupTest()
        bindSingleton(configHolder)
    }

    @BeforeEach
    fun setup() {
        mapsRecipe = MapsRecipe(configHolder)
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
                mapsRecipe.tryToProcess(
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
        configFile.writeText(mapsConfig.encodeToString())

        //then
        assertThrows<FileNotFoundException> {
            runTest {
                mapsRecipe.tryToProcess(
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
                mapsRecipe.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        //given
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText(mapsConfig.encodeToString())

        //then
        assertThrows<FileNotFoundException> {
            runTest {
                mapsRecipe.tryToProcess(
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
        configFile.writeText(mapsConfig.encodeToString())

        //when
        assertDoesNotThrow {
            runTest {
                mapsRecipe.tryToProcess(
                    sourceDirectory,
                    outputDirectory
                )
            }
        }

        //then
        assertThat(File(outputDirectory, "config.json").exists()).isTrue
    }

    @Test
    fun tryToApply_shouldThrow_withNonExistingContent() {
        assertThrows<FileNotFoundException> {
            runTest {
                mapsRecipe.tryToApply(File(""))
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
                mapsRecipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfig() {
        //given
        val configFile = File(sourceDirectory, "config.json")
        mapsConfig.writeToPath(configFile)

        //then
        assertDoesNotThrow {
            runTest {
                mapsRecipe.tryToApply(sourceDirectory)
            }
        }

        val expectedResult = mapsConfig.locations.entries.map {
            LocationData(
                itemId = it.key,
                name = it.value.name,
                subtitle = it.value.subtitle,
                address = it.value.address,
                images = emptyList(),
                description = it.value.description,
                bottomWidgetCollection = it.value.bottomWidgetCollection,
                tags = emptyList(),
                order = it.value.order,
            )
        }
        assertThat(expectedResult).usingRecursiveComparison().isEqualTo(configHolder.getLocations())
    }
}
