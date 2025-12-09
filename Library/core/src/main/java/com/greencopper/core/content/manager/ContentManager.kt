package com.greencopper.core.content.manager

import com.greencopper.core.content.recipe.ContentRecipe
import kotlinx.coroutines.flow.Flow

public interface ContentManager {

    public val contents: Set<Content>
    public val currentContent: Content?
    public val currentContentFlow: Flow<Content?>
    public val forcedContent: Content?
    public val registeredRecipes: Set<ContentRecipe>
    public val previousProjects: Set<String>

    public val isApplyingContent: Flow<Boolean>

    /** Eligible [Content] with the highest version that is ready to be applied */
    public val contentToApply: Content?

    public fun contentToApply(project: String): Content?

    public fun eligibleContentsToApply(project: String): Set<Content>

    /** Register a [ContentRecipe] to be processed or applied*/
    public fun register(recipe: ContentRecipe)

    /** Process [content] data from archive to proper directory */
    public suspend fun process(content: Content, saveInHistory: Boolean = true): Content

    /** Apply [content] data to components */
    public suspend fun apply(content: Content, forceApply: Boolean = false): Content

    public fun releaseForcedContent(): Flow<Content>?

    public suspend fun releaseForcedContentAtLaunch()
}

public fun ContentManager.register(vararg recipes: ContentRecipe) {
    recipes.forEach { register(it) }
}

public fun ContentManager.register(vararg recipes: List<ContentRecipe>) {
    recipes.toList().flatten().forEach { recipe ->
        register(
            recipe
        )
    }
}
