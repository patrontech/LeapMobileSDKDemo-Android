package com.greencopper.testmocks.core

import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.content.recipe.ContentRecipe
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.coroutines.flow.Flow

class MockContentManager(
    public var currentContentValue: () -> Content? = { unimplemented() },
    public var currentContentFlowValue: () -> Flow<Content?> = { unimplemented() },
    public var forcedContentValue: () -> Content? = { unimplemented() },
    public var previousProjectsValue: () -> Set<String> = { unimplemented() },
    public var isApplyingContentValue: () -> Flow<Boolean> = { unimplemented() },
    public var contentsValue: () -> Set<Content> = { unimplemented() },
    public var registerValue: (ContentRecipe) -> Unit = { unimplemented() },
    public var processValue: (Content) -> Content = { unimplemented() },
    public var applyValue: (Content, Boolean) -> Content = { content, _ -> content },
    public var registeredRecipesValue: () -> Set<ContentRecipe> = { unimplemented() },
    public var contentToApplyValue: () -> Content? = { unimplemented() },
    public var eligibleContentsToApplyValue: () -> Set<Content> = { unimplemented() },
    public var releaseForcedContentValue: () -> Flow<Content>? = { unimplemented() },
) : ContentManager {

    override val contentToApply: Content?
        get() = contentToApplyValue()

    override val contents: Set<Content>
        get() = contentsValue()

    override val currentContent: Content?
        get() = currentContentValue()

    override val currentContentFlow: Flow<Content?>
        get() = currentContentFlowValue()

    override val forcedContent: Content?
        get() = forcedContentValue()

    override val previousProjects: Set<String>
        get() = previousProjectsValue()

    override val isApplyingContent: Flow<Boolean>
        get() = isApplyingContentValue()

    override val registeredRecipes: Set<ContentRecipe>
        get() = registeredRecipesValue()

    override fun contentToApply(project: String): Content? = contentToApplyValue()

    override fun eligibleContentsToApply(project: String): Set<Content> =
        eligibleContentsToApplyValue()

    override fun register(recipe: ContentRecipe): Unit = registerValue(recipe)

    override suspend fun process(content: Content, saveInHistory: Boolean): Content =
        processValue(content)

    override suspend fun apply(content: Content, forceApply: Boolean): Content =
        applyValue(content, forceApply)

    override fun releaseForcedContent(): Flow<Content>? = releaseForcedContentValue()

    override suspend fun releaseForcedContentAtLaunch(): Unit = Unit
}
