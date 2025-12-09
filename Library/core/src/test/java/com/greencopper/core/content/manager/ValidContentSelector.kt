package com.greencopper.core.content.manager

import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.recipe.CoreConfiguration

internal class ValidContentSelector(
    private val contentsToCleanValue: Set<Content> = emptySet(),
    private val eligibleContentsToApplyValue: Set<Content> = emptySet(),
    private val contentToApplyValue: Content? = null,
    private val activeProjectsValue: Set<String> = emptySet(),
    private val projectsBeforeValue: Set<String> = emptySet(),
) : ContentSelector {
    override fun contentsToClean(
        contents: Set<Content>,
        registeredRecipes: Set<ContentRecipeKey>,
        currentContent: Content?,
        forcedContent: Content?,
        contentConfig: CoreConfiguration.ContentConfig
    ): Set<Content> = contentsToCleanValue

    override fun eligibleContentsToApply(
        contents: Set<Content>,
        registeredRecipes: Set<ContentRecipeKey>,
        project: String
    ): Set<Content> = eligibleContentsToApplyValue

    override fun contentToApply(
        contents: Set<Content>,
        registeredRecipes: Set<ContentRecipeKey>,
        project: String,
        currentContent: Content?,
        forcedContent: Content?
    ): Content? = contentToApplyValue

    override fun activeProjects(contents: Set<Content>): Set<String> = activeProjectsValue

    override fun projectsBefore(projectTag: String?, contents: Set<Content>) = projectsBeforeValue
}