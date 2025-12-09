package com.greencopper.core.content.manager

import com.greencopper.core.content.recipe.ContentRecipeInfo
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.recipe.CoreConfiguration

public interface ContentSelector {
    public fun contentsToClean(
        contents: Set<Content>,
        registeredRecipes: Set<ContentRecipeKey>,
        currentContent: Content? = null,
        forcedContent: Content? = null,
        contentConfig: CoreConfiguration.ContentConfig
    ): Set<Content>

    public fun eligibleContentsToApply(
        contents: Set<Content>,
        registeredRecipes: Set<ContentRecipeKey>,
        project: String
    ): Set<Content>

    public fun contentToApply(
        contents: Set<Content>,
        registeredRecipes: Set<ContentRecipeKey>,
        project: String,
        currentContent: Content? = null,
        forcedContent: Content? = null
    ): Content?

    public fun activeProjects(contents: Set<Content>): Set<String>

    public fun projectsBefore(projectTag: String?, contents: Set<Content>): Set<String>
}

public fun Collection<ContentRecipeKey>.intersectionRegardlessImplementation(infoSet: Set<ContentRecipeInfo>): Set<ContentRecipeKey> {
    return filter { infoSet.contains(it.info) }.toSet()
}