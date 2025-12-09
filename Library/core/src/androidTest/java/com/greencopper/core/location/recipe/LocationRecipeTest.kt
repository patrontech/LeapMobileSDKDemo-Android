package com.greencopper.core.location.recipe

import android.location.Location
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.data.writeToPath
import com.greencopper.core.location.LocationConfigurationHolder
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileNotFoundException

internal class LocationRecipeTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var locationRecipe: LocationRecipe
    private val locationConfigurationHolder = LocationConfigurationHolder()
    private lateinit var sourceDirectory: File
    private lateinit var outputDirectory: File

    private val locationConfig = createLocationConfig()

    init {
        Toolkit.setupTest(applicationContext = context)

        bindSingleton(locationConfigurationHolder)
    }

    @BeforeEach
    fun setup() {
        locationRecipe = LocationRecipe(locationConfigurationHolder)
        sourceDirectory = context.getDir("source", 0)
        outputDirectory = context.getDir("output", 0)
    }

    @AfterEach
    fun cleanUp() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun componentPath() {
        assertThat(locationRecipe.componentPath).isEqualTo("core/location")
    }

    @Test
    fun tryToProcess_shouldThrow_withSourceFileNotBeingADirectory() {
        //given
        val fileNotBeingADirectory = File("test.txt")

        //then
        assertThrows<IllegalArgumentException> {
            runTest {
                locationRecipe.tryToProcess(
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
        configFile.writeText(locationConfig.encodeToString())

        //then
        assertThrows<FileNotFoundException> {
            runTest {
                locationRecipe.tryToProcess(
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
                locationRecipe.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        //given
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText(locationConfig.encodeToString())

        //then
        assertThrows<FileNotFoundException> {
            runTest {
                locationRecipe.tryToProcess(
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
        configFile.writeText(locationConfig.encodeToString())

        //when
        assertDoesNotThrow {
            runTest {
                locationRecipe.tryToProcess(
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
                locationRecipe.tryToApply(File(""))
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
                locationRecipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfig() {
        //given
        val configFile = File(sourceDirectory, "config.json")
        locationConfig.writeToPath(configFile)

        //then
        assertDoesNotThrow {
            runTest {
                locationRecipe.tryToApply(sourceDirectory)
            }
        }

        assertThat(locationConfig.regions[0]).usingRecursiveComparison().isEqualTo(locationConfigurationHolder.currentConfiguration.value!!.regions[0])
    }

    private fun createLocationConfig(): LocationConfiguration {
        val location = Location("").apply {
            latitude = 28.2183326000000000
            longitude = 106.7036148330963100
        }
        return LocationConfiguration(
            accuracy = Accuracy.COARSE,
            frequency = 1,
            timeout = 1,
            regions = listOf(
                Region(
                    id = 42,
                    name = "test1",
                    location = location,
                    radiusInMeters = 500
                ),
                Region(
                    id = 43,
                    name = "test2",
                    location = location,
                    radiusInMeters = 3000
                )
            )
        )
    }
}
