package com.greencopper.core.content.projectswitcher

import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.content.ota.OTAManager

internal class ConcreteProjectSwitcher(
    private val contentManager: ContentManager,
    private val otaManager: OTAManager
) : ProjectSwitcher {
    override suspend fun switchProject(params: ProjectParams): Content? {
        if (isCurrentProject(params.project)) {
            return null
        }

        val otaContent = otaManager.otaContentToProcess(params.otaApiUrl)
        otaContent?.let { otaManager.process(it) }
        return contentManager.contentToApply(params.project)?.let {
            contentManager.apply(it)
        } ?: throw IllegalStateException("No content to apply.")
    }

    override fun isCurrentProject(project: String): Boolean =
        contentManager.currentContent?.project == project
}
