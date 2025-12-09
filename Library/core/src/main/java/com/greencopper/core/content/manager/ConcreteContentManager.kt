package com.greencopper.core.content.manager

import androidx.annotation.VisibleForTesting
import com.greencopper.core.content.recipe.*
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.toolkit.extensions.mapErrorNotType
import com.greencopper.toolkit.logging.*
import kotlinx.coroutines.flow.*

internal class ConcreteContentManager(
    private val processor: ContentProcessor,
    private val contentSelector: ContentSelector,
    private val contentHistory: ContentHistory,
    private val coreConfigurationHolder: CoreConfigurationHolder,
    private val projectCleaner: ProjectCleaner,
    private val logging: Logging,
) : ContentManager, CurrentProjectTagProvider {

    private val recipes: MutableSet<ContentRecipe> = mutableSetOf()

    private val _isApplyingContent = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 2).apply { tryEmit(false) }
    override val isApplyingContent: Flow<Boolean> = _isApplyingContent.distinctUntilChanged()

    override val contents: Set<Content>
        get() = contentHistory.contents

    override val currentContent: Content?
        get() = contentHistory.currentContent

    override val currentContentFlow: Flow<Content?>
        get() = contentHistory.currentContentFlow

    override val forcedContent: Content?
        get() = contentHistory.forcedContent

    override val registeredRecipes: Set<ContentRecipe>
        get() = recipes

    override val contentToApply: Content?
        get() = contentHistory.currentProject?.let {
            contentToApply(it)
        }

    override val currentProject: String?
        get() = contentHistory.currentProject

    override val currentProjectFlow: SharedFlow<String?>
        get() = contentHistory.currentProjectFlow


    private val contentsToClean: Set<Content>
        get() {
            return contentSelector.contentsToClean(
                contents,
                registeredRecipes.keys(),
                currentContent,
                forcedContent,
                coreConfigurationHolder.currentConfiguration.value?.contentConfig
                    ?: throw IllegalStateException()
            )
        }

    override val previousProjects: Set<String>
        get() = contentSelector.projectsBefore(currentProject, contents)

    override fun contentToApply(project: String): Content? =
        contentSelector.contentToApply(
            contents,
            registeredRecipes.keys(),
            project,
            currentContent,
            forcedContent
        )

    override fun eligibleContentsToApply(project: String): Set<Content> =
        contentSelector.eligibleContentsToApply(
            contents,
            registeredRecipes.map { it.key }.toSet(),
            project
        )

    override fun register(recipe: ContentRecipe) {
        recipes.add(recipe)
    }

    override suspend fun process(content: Content, saveInHistory: Boolean): Content {
        try {
            logging.d("Processing started for content: $content")
            if (saveInHistory) {
                insertAndSave(content)
            }
            processor.open(content)
            val result = processor.process(
                content,
                registeredRecipes.intersectionRegardlessImplementation(content.enabledRecipes)
            )

            contentHistory.saveHistory()
            logging.i("Processed content: $content")
            return result
        } catch (t: Throwable) {
            contentHistory.saveHistory()
            logging.w(message = "Processing failed for content: $content", throwable = t)
            throw t
        }
    }

    override suspend fun apply(content: Content, forceApply: Boolean): Content {
        try {
            _isApplyingContent.tryEmit(true)

            logging.d("Applying started for content: $content")
            val enabledRecipes = registeredRecipes.intersection(content.processedRecipes)
            val appliedContent = processor.apply(content, enabledRecipes)

            contentHistory.forcedContent =
                if (forceApply || appliedContent == forcedContent) appliedContent else null
            cleanObsoleteContents()
            setNewProject(appliedContent)
            resetNotAppliedRecipes(appliedContent)
            logging.i("Applied content: $content")

            _isApplyingContent.tryEmit(false)
            return content
        } catch (t: Throwable) {
            _isApplyingContent.tryEmit(false)
            logging.e(message = "Applying failed for content: $content", throwable = t)
            processor.clean(content)
            contentHistory.saveHistory()

            throw t.mapErrorNotType<ContentException> { ContentException.ProcessorApplyException(it, content) }
        }
    }

    private fun resetNotAppliedRecipes(appliedContent: Content) {
        val predicate: (ContentRecipe) -> Boolean = {
            registeredRecipes.contains(it)
                    && !appliedContent.processedRecipes.contains(it.key)
                    && it is Resettable
        }
        val registeredRecipesNotApplied = recipes.filter(predicate).map { it as Resettable }
        registeredRecipesNotApplied.forEach { it.reset() }
    }

    override fun releaseForcedContent(): Flow<Content>? =
        if (forcedContent == null) {
            null
        } else {
            flow {
                contentToApply?.let {
                    emit(apply(it))
                } ?: throw IllegalStateException("No content to apply")
            }.map {
                forcedContent?.let {
                    processor.clean(it)
                }
                insertAndSave(it)
                it
            }
        }

    override suspend fun releaseForcedContentAtLaunch() {
        forcedContent?.let {
            processor.clean(it)
            insertAndSave(it)
        }
    }

    @VisibleForTesting
    internal suspend fun cleanObsoleteContents() {
        if (coreConfigurationHolder.currentConfiguration.value?.contentConfig == null) {
            logging.e("ContentConfig should never be null here. Current content : $currentContent")
            return
        }

        contentsToClean.forEach { obsoleteContent -> processor.clean(obsoleteContent) }

        projectCleaner.cleanProjectsData(contentSelector.activeProjects(contents))
    }

    private suspend fun insertAndSave(content: Content) {
        contentHistory.saveContent(content)
    }

    protected suspend fun finalize() {
        contentHistory.saveHistory()
    }

    private fun setNewProject(content: Content) {
        // This is where the project gets switched.
        contentHistory.currentContent = content
    }

    @VisibleForTesting
    internal fun getRecipesForTest(): Set<ContentRecipe> = recipes
}
