package com.greencopper.core.content.manager

import com.greencopper.core.content.ContentAssembly
import com.greencopper.core.content.archive.ArchiveOpenerException
import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.recipe.*
import com.greencopper.core.recipe.CoreConfigRecipe
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File

internal class ConcreteContentProcessorTest {

    private lateinit var contentFileSaved: File
    private val storageManager: StorageManager
    private val contentConfiguration: RunConfiguration.Content

    init {
        Toolkit.setup(listOf(ContentAssembly()), emptyList(), null)
        storageManager = App.resolve()
        contentConfiguration = RunConfiguration.build(storageManager, App.resolve()).content
        runTest {
            contentFileSaved =
                archive.file.copyTo(
                    File(storageManager.getCacheStorage(), archive.file.name),
                    overwrite = true
                )
        }
    }

    private fun newProcessor() = ConcreteContentProcessor(
        archiveOpener = App.resolve(),
        storageManager = App.resolve(),
        contentConfig = contentConfiguration
    )

    @AfterEach
    fun tearDownEach() {
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

    private val content =
        Content(
            archive,
            contentConfiguration.version,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        )

    private val testContentRecipe = TestContentRecipe()

    @Test
    fun whenOpeningContent_withMalformedStructure_shouldThrow() {
        val wrongArchive = ContentArchive(
            runBlocking { storageManager.getAssetAsFile("content/content_malformed_version.zip") }, "UT_Salt"
        )
        val wrongContent = Content(wrongArchive, 1, 1, "defaultTag", OTAContent.Type.Release)
        val processor: ContentProcessor = newProcessor()
        val exception = assertThrows<ContentException.CouldntOpenContentException> {
            runTest { processor.open(wrongContent) }
        }
        assertThat(exception.cause).isInstanceOf(ArchiveOpenerException.MalformedArchiveException::class.java)
    }

    @Test
    fun whenOpeningContent_withWrongState_shouldThrow() {
        val processingContent = content.copy().apply {
            currentState = State.Processing()
        }
        val processor: ContentProcessor = newProcessor()
        assertThrows<ContentException.UnreadyStateException> {
            runTest { processor.open(processingContent) }
        }
    }

    @Test
    fun whenApplying_shouldSucceed() {
        val processor: ContentProcessor = newProcessor()
        runTest {
            val result = processor.open(content)

            assertThat(result).exists()
            assertThat(result).isNotEmptyDirectory
        }
    }

    @Test
    fun whenApplying_withSuccess_shouldHaveProperStates() {
        val processor: ContentProcessor = newProcessor()
        runTest {
            val recipes = setOf(testContentRecipe)
            processor.open(content)
            processor.process(content, recipes)
            val content = processor.apply(content, recipes)
            content.stateHistory.apply {
                assertThat(states).hasAtLeastOneElementOfType(State.Created::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Processing::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Opening::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Opened::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Processed::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Applying::class.java)
                assertThat(states).hasAtLeastOneElementOfType(State.Applied::class.java)
                assertThat(states).hasSize(7)
            }
        }
    }

    @Test
    fun whenApplying_withSuccess_processedInOrder() {
        val processor: ContentProcessor = newProcessor()
        runTest {
            val recipeFactories = setOf(testContentRecipe)
            processor.open(content)
            processor.process(content, recipeFactories)
            val content = processor.apply(content, recipeFactories)
            content.stateHistory.states.apply {
                val createdType = first { it is State.Created }
                val processingType = first { it is State.Processing }
                val openingType = first { it is State.Opening }
                val openedType = first { it is State.Opened }
                val processedType = first { it is State.Processed }
                val applyingType = first { it is State.Applying }
                val appliedType = first { it is State.Applied }
                assertThat(createdType.date).isBeforeOrEqualTo(openingType.date)
                assertThat(openingType.date).isBeforeOrEqualTo(openedType.date)
                assertThat(openedType.date).isBeforeOrEqualTo(processingType.date)
                assertThat(processingType.date).isBeforeOrEqualTo(processedType.date)
                assertThat(processedType.date).isBeforeOrEqualTo(applyingType.date)
                assertThat(applyingType.date).isBeforeOrEqualTo(appliedType.date)
            }
        }
    }

    @Test
    fun whenApplyingProcessedContent_withSuccess_recipeKeysMatch() {
        val processor: ContentProcessor = newProcessor()
        runTest {
            val recipes = setOf(testContentRecipe)
            content.currentState = State.Processed(recipes.map { it.key }.toSet())
            val content = processor.apply(content, recipes)
            content.stateHistory.states.apply {
                val processedType = first { it is State.Processed }
                val appliedType = first { it is State.Applied }

                assertThat((processedType as State.Processed).processedRecipeKeys).isEqualTo(
                    recipes.keys()
                )
                assertThat((appliedType as State.Applied).recipeKeys).isEqualTo(recipes.keys())
            }
        }
    }

    @Test
    fun whenApplyingAppliedContent_withSuccess_recipeKeysMatch() {
        val processor: ContentProcessor = newProcessor()
        runTest {
            val recipes = setOf(testContentRecipe)
            content.currentState = State.Processed(recipes.map { it.key }.toSet())
            content.currentState = State.Applied(recipes.map { it.key }.toSet())
            val content = processor.apply(content, recipes)
            content.stateHistory.states.apply {
                val processedType = first { it is State.Processed }
                val appliedType = first { it is State.Applied }

                assertThat((processedType as State.Processed).processedRecipeKeys).isEqualTo(
                    recipes.keys()
                )
                assertThat((appliedType as State.Applied).recipeKeys).isEqualTo(recipes.keys())
            }
        }
    }

    @Test
    fun whenApplying_withoutProcessing_shouldThrow() {
        val processor: ContentProcessor = newProcessor()
        val recipeFactories = setOf(testContentRecipe)
        assertThrows<ContentException.UnreadyStateException> {
            runTest { processor.apply(content, recipeFactories) }
        }
    }

    @Test
    fun whenApplying_withWrongSchema_shouldThrow() {
        val processor: ContentProcessor = newProcessor()
        runTest {
            val recipes = setOf(testContentRecipe)
            val result = processor.open(content)
            assertThat(result).isNotEmptyDirectory

            processor.process(content, recipes)
            val wrongSchemaContent = content.copy(
                content.archive,
                contentConfiguration.version,
                contentConfiguration.schema + 1
            )
            wrongSchemaContent.stateHistory.append(content.currentState)
            assertThrows<ContentException.SchemaNotMatchingException> {
                processor.apply(wrongSchemaContent, recipes)
            }
        }
    }

    @Test
    fun whenApplying_withDifferentRecipe_shouldThrow() {

        with(App.resolve<Registrar>()) {
            bindSingleton { CoreConfigurationHolder() }
            bindRecipe(auto(::CoreConfigRecipe))
        }
        val processor: ContentProcessor = newProcessor()
        val recipes: MutableSet<ContentRecipe> =
            mutableSetOf(testContentRecipe)
        runTest {
            val result = processor.open(content)
            assertThat(result).isNotEmptyDirectory

            processor.process(content, recipes)
        }
        val coreRecipeFactory = App.resolve<CoreConfigRecipe>(tag = "recipe")
        recipes.add(coreRecipeFactory)
        assertThrows<ContentException.RecipesNotMatchingException> {
            runTest { processor.apply(content, recipes) }
        }
    }

    @Test
    fun whenApplying_withUnreadyState_shouldThrow() {
        val processor: ContentProcessor = newProcessor()
        val recipes = mutableSetOf(testContentRecipe)
        runTest {
            val result = processor.open(content)
            assertThat(result).exists()

            processor.process(content, recipes)
        }
        val failRecipe = TestContentRecipe(failRun = true)
        recipes.clear()
        recipes.add(failRecipe)
        assertThrows<ContentException.AlreadyProcessedException> {
            runTest { processor.process(content, recipes) }
        }
        assertThrows<ContentException.UnreadyStateException> {
            runTest { processor.apply(content, recipes) }
        }
    }

    @Test
    fun whenApplying_withFailingRecipe_shouldThrow() {
        val processor: ContentProcessor = newProcessor()
        val recipes = setOf(TestContentRecipe(failApply = true))
        runTest {
            val result = processor.open(content)
            assertThat(result).isNotEmptyDirectory

            processor.process(content, recipes)
        }
        runTest {
            val exception = assertThrows<ContentException.RecipeException> {
                processor.apply(content, recipes)
            }
            assertThat(exception.cause).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    @DisplayName("Given a valid content, When calling clean, Then content state is Cleaned")
    fun cleanShouldSucceed() {
        val processor: ContentProcessor = newProcessor()
        val contentToClean = content.copy()
        contentToClean.currentState = State.Processed(setOf())
        runTest {
            processor.clean(contentToClean)
            assertThat(contentToClean.currentState).isInstanceOf(State.Cleaned::class.java)
        }
    }
}
