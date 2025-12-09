package com.greencopper.core.content.manager

import com.greencopper.core.content.archive.ConcreteContentArchiveOpener
import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.recipe.*
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.testing.TemporaryStorageManager
import com.greencopper.toolkit.zip.Zip4jClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class ConcreteContentProcessorTestProcess : CoroutineTest(UnconfinedTestDispatcher()) {

    private val processor: ConcreteContentProcessor

    private lateinit var contentFileSaved: File
    private val storageManager = TemporaryStorageManager()
    private val contentConfiguration: RunConfiguration.Content

    init {
        Toolkit.setupTest()
        contentConfiguration = RunConfiguration.build(storageManager, App.resolve()).content
        val archiveOpener = ConcreteContentArchiveOpener(
            zipClient = Zip4jClient(testScope),
            json = App.resolve(),
        )
        processor = ConcreteContentProcessor(
            archiveOpener = archiveOpener,
            storageManager = storageManager,
            contentConfig = contentConfiguration,
        )

        runTest {
            contentFileSaved =
                archive.file.copyTo(File(storageManager.getCacheStorage(), archive.file.name), overwrite = true)
        }
    }

    override fun afterEach() {
        runTest {
            if (!archive.file.exists()) {
                File(storageManager.getCacheStorage(), archive.file.name).copyTo(archive.file)
            }
        }
    }

    private val archive
        get() = ContentArchive(
            runBlocking { storageManager.getAssetAsFile("content/${contentConfiguration.fileName}") },
            contentConfiguration.secret
        )

    private val content = Content(
        archive,
        contentConfiguration.version,
        contentConfiguration.schema,
        contentConfiguration.project,
        OTAContent.Type.Release,
    )

    private val testContentRecipe = TestContentRecipe()

    @Test
    fun whenProcessingContent_resultIsNotEmpty() {
        runTest {
            val result = processor.open(content)
            assertThat(result).isNotEmptyDirectory
        }
    }

    @Test
    fun whenProcessingContent_withSuccess_hasCorrectStates() {
        runTest {
            val recipeFactories = setOf(testContentRecipe)
            processor.open(content)
            val processedContent = processor.process(content, recipeFactories)
            assertThat(processedContent.currentState).isInstanceOf(State.Processed::class.java)

            with (processedContent.stateHistory) {
                assertThat(states).hasAtLeastOneElementOfType(State.Created::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Opening::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Opened::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Processing::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Processed::class.java)
                assertThat(states).hasSize(5)
            }
        }
    }

    @Test
    fun whenProcessingContent_withSuccess_processedInOrder() {
        runTest {
            val recipeFactories = setOf(testContentRecipe)
            processor.open(content)
            val processedContent = processor.process(content, recipeFactories)
            with (processedContent.stateHistory.states) {
                val createdType = first { it is State.Created }
                val openingType = first { it is State.Opening }
                val openedType = first { it is State.Opened }
                val processingType = first { it is State.Processing }
                val processedType = first { it is State.Processed }
                assertThat(createdType.date).isBeforeOrEqualTo(openingType.date)
                assertThat(openingType.date).isBeforeOrEqualTo(openedType.date)
                assertThat(openedType.date).isBeforeOrEqualTo(processingType.date)
                assertThat(processingType.date).isBeforeOrEqualTo(processedType.date)
                assertThat((processedType as State.Processed).processedRecipeKeys).isEqualTo(
                    recipeFactories.keys()
                )
            }
        }
    }

    @Test
    fun whenProcessingContent_withSuccess_recipeKeysMatch() {
        runTest {
            val recipeFactories = setOf(testContentRecipe)
            processor.open(content)
            val processedContent = processor.process(content, recipeFactories)
            processedContent.stateHistory.states.apply {
                val processedType = first { it is State.Processed }
                assertThat((processedType as State.Processed).processedRecipeKeys).isEqualTo(
                    recipeFactories.keys()
                )
            }
        }
    }

    @Test
    fun whenProcessingContent_withoutRecipes_shouldThrow() {
        val recipes = setOf<ContentRecipe>()
        runTest {
            val result = processor.open(content)
            assertThat(result).isNotEmptyDirectory

        }
        val exception = assertThrows<ContentException.NoRecipeRegisteredException> {
            runTest { processor.process(content, recipes) }
        }
        assertThat(exception).hasMessage("[ContentException] Trying to process content without any Recipe")
    }

    @Test
    fun whenProcessingContent_withFailingRecipe_shouldThrow() {
        val recipeFactories = setOf(TestContentRecipe(failRun = true))
        runTest {
            val result = processor.open(content)
            assertThat(result).isNotEmptyDirectory
        }
        assertThrows<ContentException.RecipeException> {
            runTest { processor.process(content, recipeFactories) }
        }
    }

    @Test
    fun whenProcessingContent_alreadyProcessed_shouldThrow() {
        val recipeFactories = setOf(testContentRecipe)
        runTest {
            val result = processor.open(content)
            assertThat(result).isNotEmptyDirectory

            processor.process(content, recipeFactories)
        }
        assertThrows<ContentException.AlreadyProcessedException> {
            runTest { processor.process(content, recipeFactories) }
        }
    }

    @Test
    fun whenProcessingContent_whenStateCleaned_shouldThrow() {
        val recipeFactories = setOf(testContentRecipe)
        val cleanContent = content.copy()
        cleanContent.currentState = State.Cleaned()
        assertThrows<ContentException.AlreadyProcessedException> {
            runTest { processor.process(cleanContent, recipeFactories) }
        }
    }

    @Test
    fun whenProcessingContent_whenStateApplied_shouldThrow() {
        runTest {
            val recipeFactories = setOf(testContentRecipe)
            val result = processor.open(content)
            assertThat(result).isNotEmptyDirectory

            processor.process(content, recipeFactories)
            processor.apply(content, recipeFactories)
            assertThrows<ContentException.AlreadyProcessedException> {
                processor.process(content, recipeFactories)
            }
        }
    }

    @Test
    fun whenProcessingContent_whenStateProcessingFailed_shouldThrow() {
        val recipeFactories = setOf(TestContentRecipe(failRun = true))
        runTest {
            val result = processor.open(content)
            assertThat(result).isNotEmptyDirectory
        }
        assertThrows<ContentException.RecipeException> {
            runTest { processor.process(content, recipeFactories) }
        }
        assertThrows<ContentException.AlreadyProcessedException> {
            runTest { processor.process(content, recipeFactories) }
        }
    }

    @Test
    fun whenProcessing_beforeOpening_shouldThrow() {
        val recipeFactories = setOf(testContentRecipe)
        assertThrows<ContentException.ContentNotOpenedException> {
            runTest { processor.process(content, recipeFactories) }
        }
    }

    @Test
    fun whenProcessingContent_whenStateOpeningFailed_shouldThrow() {
        val recipeFactories = setOf(testContentRecipe)
        val wrongPasswordContent = content.copy(archive = content.archive.copy(secret = "wrongSecret"))
        assertThrows<ContentException.CouldntOpenContentException> {
            runTest { processor.open(wrongPasswordContent) }
        }
        assertThat(wrongPasswordContent.currentState).isInstanceOf(State.OpeningFailed::class.java)
        assertThrows<ContentException.ContentNotOpenedException> {
            runTest { processor.process(wrongPasswordContent, recipeFactories) }
        }
    }

    @Test
    fun whenProcessingContent_whenStateOpening_shouldThrow() {
        val recipeFactories = setOf(testContentRecipe)
        val openingState = content.copy()
        openingState.currentState = State.Opening()
        assertThrows<ContentException.ContentNotOpenedException> {
            runTest { processor.process(openingState, recipeFactories) }
        }
    }

    @Test
    fun whenProcessingContent_whenStateProcessing_shouldThrow() {
        val recipeFactories = setOf(testContentRecipe)
        val processingState = content.copy()
        processingState.currentState = State.Processing()
        assertThrows<ContentException.AlreadyProcessedException> {
            runTest { processor.process(processingState, recipeFactories) }
        }
    }

    @Test
    fun whenProcessingContent_whenApplyingFailed_shouldThrow() {
        val recipeFactories = setOf(testContentRecipe)
        val applyingFailedState = content.copy()
        applyingFailedState.currentState = State.ApplyingFailed()
        assertThrows<ContentException.AlreadyProcessedException> {
            runTest { processor.process(applyingFailedState, recipeFactories) }
        }
    }

    @Test
    fun whenProcessingContent_whenStateApplying_shouldThrow() {
        val recipeFactories = setOf(testContentRecipe)
        val applyingState = content.copy()
        applyingState.currentState = State.Applying()
        assertThrows<ContentException.AlreadyProcessedException> {
            runTest { processor.process(applyingState, recipeFactories) }
        }
    }
}
