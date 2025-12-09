package com.greencopper.core.content.manager

import com.greencopper.core.content.archive.ContentArchiveOpener
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.recipe.*
import com.greencopper.toolkit.App
import com.greencopper.toolkit.extensions.mapErrorNotType
import com.greencopper.toolkit.logging.d
import com.greencopper.toolkit.storage.StorageManager
import java.io.File

internal class ConcreteContentProcessor(
    private val archiveOpener: ContentArchiveOpener,
    private val storageManager: StorageManager,
    private val contentConfig: RunConfiguration.Content
) : ContentProcessor {

    private val contentDirPath = "contents"

    private suspend fun getArchiveContentDirectory(content: Content) = File(
        File(storageManager.getProjectFilesStorage(content.project), "$contentDirPath/${content.version}"),
        "archive_content"
    ).apply { mkdirs() }

    private suspend fun getVersionDirectory(content: Content, recipeKeys: Set<ContentRecipeKey>) =
        File(storageManager.getProjectFilesStorage(content.project), "$contentDirPath/${content.schema}_${content.version}_${recipeKeys.hashCode()}")

    private suspend fun getBuiltContentDirectory(content: Content, recipeKeys: Set<ContentRecipeKey>) = File(
        getVersionDirectory(content, recipeKeys),
        "built_content"
    ).apply { mkdirs() }


    override suspend fun open(content: Content): File {
        try {
            checkToOpen(content)
            content.currentState = State.Opening()
            val file = archiveOpener.open(
                content.archive,
                content.version, contentConfig.schema,
                getArchiveContentDirectory(content)
            )

            val enabledRecipes = ContentConfiguration.fromDirectory(getArchiveContentDirectory(content))
                .recipes
                .filter { it.enabled }
                .map { it.key }
                .toSet()
            content.currentState = State.Opened(enabledRecipes = enabledRecipes)

            return file
        } catch (t: Throwable) {
            content.currentState = State.OpeningFailed()
            throw t.mapErrorNotType<ContentException> { ContentException.CouldntOpenContentException(it) }
        }
    }

    private fun checkToOpen(content: Content) {
        checkSchema(content)
        if (content.currentState !is State.Created) {
            throw ContentException.UnreadyStateException(content.currentState)
        }
    }

    override suspend fun process(
        content: Content,
        recipes: Set<ContentRecipe>
    ): Content {
        try {
            processOpenedContent(recipes, content)

            getArchiveContentDirectory(content).config()
                .copyTo(getBuiltContentDirectory(content, recipes.keys()).config(), overwrite = true)
            content.currentState = State.Processed(recipes.keys())
            getArchiveContentDirectory(content).deleteRecursively()
        } catch (t: Throwable) {
            content.currentState = State.ProcessingFailed()
            getVersionDirectory(content, content.processedRecipes).deleteRecursively()
            throw t.mapErrorNotType<ContentException> { ContentException.ProcessorProcessException(it, content) }
        }

        return content
    }

    private suspend fun processOpenedContent(
        recipes: Set<ContentRecipe>,
        content: Content
    ): Content {
        checkToProcess(content, recipes)
        content.currentState = State.Processing()

        val unzippedDirectory = getArchiveContentDirectory(content)
        val processedDirectory = getBuiltContentDirectory(content, recipes.keys())
        processedDirectory.listFiles()?.forEach {
            it.deleteRecursively()
        }

        recipes.forEach { recipe ->
            processAsync(recipe, unzippedDirectory, processedDirectory)
        }

        return content
    }

    private suspend fun processAsync(
        recipe: ContentRecipe,
        unzipContent: File,
        processedDirectory: File
    ) {
        val unzipContentPath = File(unzipContent, recipe.componentPath)
        val processedContentPath =
            File(processedDirectory, recipe.componentPath).apply { mkdirs() }
        recipe.process(unzipContentPath, processedContentPath)
    }

    private fun checkToProcess(
        content: Content,
        recipes: Set<ContentRecipe>
    ) {
        when (content.currentState) {
            is State.Processing, is State.ProcessingFailed, is State.Processed, is State.Applying, is State.ApplyingFailed, is State.Applied, is State.Cleaned -> throw ContentException.AlreadyProcessedException()
            is State.Opened -> {
                // This is the correct state to process
            }
            is State.Created, is State.Opening, is State.OpeningFailed -> throw ContentException.ContentNotOpenedException()
        }
        checkRecipesRegistered(recipes)
        checkSchema(content)
    }

    private fun checkRecipesRegistered(recipes: Set<ContentRecipe>) {
        if (recipes.isEmpty()) {
            throw ContentException.NoRecipeRegisteredException()
        }
    }

    override suspend fun apply(
        content: Content,
        recipes: Set<ContentRecipe>
    ): Content {
        try {
            checkToApply(content, recipes)
            content.currentState = State.Applying()

            recipes.forEach { recipe ->
                applyAsync(recipe, getBuiltContentDirectory(content, recipes.keys()))
            }

            content.currentState = State.Applied(recipes.keys())
            return content
        } catch (t: Throwable) {
            content.currentState = State.ApplyingFailed()
            throw t.mapErrorNotType<ContentException> { ContentException.ProcessorApplyException(it, content) }
        }

    }

    private suspend fun applyAsync(
        recipe: ContentRecipe,
        contentDirectory: File
    ) {
        val contentDirectoryPath = File(contentDirectory, recipe.componentPath).apply { mkdirs() }
        recipe.apply(contentDirectoryPath)
    }

    override suspend fun clean(content: Content) {
        getVersionDirectory(content, content.processedRecipes).deleteRecursively()
        content.archive.file.delete()
        content.currentState = State.Cleaned()
        App.log.d("Cleaned content $content")

        cleanOldFormatContent(content)
    }

    //Meant to clean all the content folders using "content" as parent directory
    //The new parent directory name is "contents"
    private suspend fun cleanOldFormatContent(content: Content) {
        File(storageManager.getProjectFilesStorage(content.project), "content").deleteRecursively()
    }

    private fun checkSchema(content: Content) {
        if (content.schema != contentConfig.schema) {
            throw ContentException.SchemaNotMatchingException(
                actual = content.schema,
                expected = contentConfig.schema
            )
        }
    }

    private fun checkToApply(content: Content, recipes: Set<ContentRecipe>) {
        when (val currentState = content.currentState) {
            is State.Applied, is State.Processed -> {
                checkSchema(content)
                checkRecipesRegistered(recipes)
                val recipeKeys = content.stateHistory.processedRecipeKeys
                    ?: throw IllegalStateException("Processed recipe keys shouldn't be null in $content")
                if (recipeKeys != recipes.keys()) {
                    throw ContentException.RecipesNotMatchingException(
                        recipeKeys,
                        recipes.keys()
                    )
                }
            }
            else -> throw ContentException.UnreadyStateException(currentState)
        }
    }

    private fun File.config(): File = File(this, "config.json")
}
