package com.greencopper.core.content.manager

import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.draftcontent.DraftContentManager
import com.greencopper.core.recipe.CoreConfiguration
import java.time.Duration
import java.time.Instant

internal class ConcreteContentSelector(
    private val currentSchema: ContentSchema,
    private val draftContentManager: DraftContentManager,
) : ContentSelector {

    private val applyingExpirationDuration = Duration.ofMinutes(10)

    override fun contentsToClean(
        contents: Set<Content>,
        registeredRecipes: Set<ContentRecipeKey>,
        currentContent: Content?,
        forcedContent: Content?,
        contentConfig: CoreConfiguration.ContentConfig
    ): Set<Content> {
        // Don't clean anything if no content has been applied yet or if there is a forcedContent
        if (currentContent == null && forcedContent == null) return emptySet()

        val contentToApply = contentToApply(
            contents,
            registeredRecipes,
            currentContent?.project ?: forcedContent?.project
            ?: throw IllegalStateException("No valid content has been found."),
            currentContent,
            forcedContent
        )

        return contents.filter { content ->
            when {
                // Clean contents that were draft if we're not in draft mode anymore
                content.type == OTAContent.Type.Draft && draftContentManager.passcode == null -> true
                // Escape already cleaned contents (have to be escaped before expiration check)
                content.currentState is State.Cleaned -> false
                // Don't clean currentContent or contentToApply
                content == currentContent || content == contentToApply -> false
                // Clean contents with unsupported schema
                content.schema != currentSchema -> false
                // Clean contents older than expiration timeInterval
                content.currentState.date.plus(Duration.ofSeconds(contentConfig.expiration))
                    .isBefore(Instant.now()) -> true
                // Clean contents from deprecated projects
                contentConfig.deprecatedProjects.contains(content.project) -> true
                else -> filterContentsFromState(content, currentContent)
            }
        }.toSet()
    }

    private fun filterContentsFromState(content: Content, currentContent: Content?): Boolean =
        when (content.currentState) {
            is State.Created, is State.Opening, is State.Opened, is State.Processing, is State.Applying -> {
                // Clean ongoing contents older than ongoingExpiration timeInterval
                content.currentState.date.plus(applyingExpirationDuration).isBefore(Instant.now())
            }
            is State.OpeningFailed, is State.ProcessingFailed, is State.ApplyingFailed, is State.Cleaned ->
                // Clean failed contents
                // (cleaned is here just for coverage, it has already been early escaped)
                true
            is State.Processed, is State.Applied ->
                // If we reach this case, only clean for the current project contents
                content.project == currentContent?.project
        }

    override fun eligibleContentsToApply(
        contents: Set<Content>,
        registeredRecipes: Set<ContentRecipeKey>,
        project: String
    ): Set<Content> {
        return contents.filter {
            when {
                it.schema != currentSchema || it.project != project -> false
                it.type == OTAContent.Type.Draft && draftContentManager.passcode == null -> false
                (it.currentState is State.Processed || it.currentState is State.Applied) &&
                        it.processedRecipes.isNotEmpty() && it.enabledRecipes.isNotEmpty() -> {
                    val processableRecipes =
                        registeredRecipes.intersectionRegardlessImplementation(it.enabledRecipes)
                    processableRecipes.containsAll(it.processedRecipes)
                }
                else -> false
            }
        }.sortedByDescending { it.version }.toSet()
    }

    override fun contentToApply(
        contents: Set<Content>,
        registeredRecipes: Set<ContentRecipeKey>,
        project: String,
        currentContent: Content?,
        forcedContent: Content?
    ): Content? {
        forcedContent?.let {
            return null
        }

        val eligibleContent =
            eligibleContentsToApply(contents, registeredRecipes, project).firstOrNull()
        return if (eligibleContent != currentContent) {
            eligibleContent
        } else {
            null
        }
    }

    override fun activeProjects(contents: Set<Content>): Set<String> =
        contents.filter { it.currentState !is State.Cleaned }
            .map { it.project }
            .toSet()

    override fun projectsBefore(projectTag: String?, contents: Set<Content>): Set<String> =
        contents
            .asSequence()
            .filter { it.project != projectTag }
            .mapNotNull { content ->
                (content.currentState as? State.Applied)?.date?.let { applyDate ->
                    content.project to applyDate
                }
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .toSet()
}
