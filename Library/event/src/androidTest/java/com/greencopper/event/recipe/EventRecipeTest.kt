package com.greencopper.event.recipe

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.data.writeToPath
import com.greencopper.event.EventDataProcessor
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File

internal class EventRecipeTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var sourceDirectory: File
    private lateinit var outputDirectory: File

    private val successEventDataProcess = object : EventDataProcessor {
        var isApplied = false

        override suspend fun process(unarchivedDirectory: File, contentDirectory: File) = Unit

        override suspend fun apply(contentDirectory: File) {
            isApplied = true
        }
    }

    private val failEventDataProcessor = object : EventDataProcessor {
        override suspend fun process(unarchivedDirectory: File, contentDirectory: File) =
            throw IllegalStateException()

        override suspend fun apply(contentDirectory: File) =
            throw IllegalArgumentException()
    }

    private val configHolder = EventConfigurationHolder()
    private val eventConfig = EventConfiguration(
        reminders = EventConfiguration.Reminders(
            topBarIcon = "icon",
            timeIntervals = listOf(EventConfiguration.TimeInterval("label", 10)),
            defaultTimeInterval = 0,
            onFirstAddToMyScheduleRouteLink = "deeplink://route",
            onNotificationTapRouteLink = "deeplink://route2"
        )
    )
    private val eventRecipe = EventRecipe(successEventDataProcess, configHolder)

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        sourceDirectory = context.getDir("source", 0)
        outputDirectory = context.getDir("output", 0)

        File(sourceDirectory, "data").createNewFile()
        val configFile = File(sourceDirectory, "config.json")
        eventConfig.writeToPath(configFile)
    }

    @AfterEach
    fun afterEach() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    @DisplayName("Given database's process and apply succeed, When tryToProcess is called, Then no exception is raised")
    fun tryToProcessShouldSucceed() {
        val classUnderTest = EventRecipe(successEventDataProcess, EventConfigurationHolder())
        assertDoesNotThrow {
            runTest {
                classUnderTest.tryToProcess(sourceDirectory, outputDirectory)
            }
        }
    }

    @Test
    @DisplayName("Given database's process and apply succeed, When tryToApply is called, Then event data processor process is called")
    fun tryToApplyShouldSucceed() {
        runTest {
            eventRecipe.tryToProcess(sourceDirectory, outputDirectory)
            eventRecipe.tryToApply(outputDirectory)
            assertThat(successEventDataProcess.isApplied).isTrue
        }
    }

    @Test
    @DisplayName("Given database's process and apply fail, When tryToProcess is called, Then IllegalStateException is raised")
    fun tryToProcessShouldThrow() {
        val classUnderTest = EventRecipe(failEventDataProcessor, configHolder)
        assertThrows<IllegalStateException> {
            runTest {
                classUnderTest.tryToProcess(sourceDirectory, outputDirectory)
            }
        }
    }

    @Test
    fun givenNoConfig_tryToApply_shouldNotThrow() {
        runTest {
            eventRecipe.tryToApply(File(""))
        }
        assertThat(configHolder.currentConfiguration.value).isNull()
    }

    @Test
    fun givenWronglyFormattedContent_tryToApply_shouldThrow() {
        //given
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText("random_content")

        //then
        assertThrows<SerializationException> {
            runTest {
                eventRecipe.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun withConfig_tryToApply_shouldSuccess() {
        assertDoesNotThrow {
            runTest {
                eventRecipe.tryToApply(sourceDirectory)
            }
        }

        assertThat(eventConfig).usingRecursiveComparison().isEqualTo(configHolder.currentConfiguration.value)
    }
}