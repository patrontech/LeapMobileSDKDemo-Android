package com.greencopper.core.content.initialcontent

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.manager.*
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.toolkit.logging.*
import com.greencopper.toolkit.storage.StorageManager

internal class ConcreteContentInitializer(
    private val manager: ContentManager,
    private val runConfigContent: RunConfiguration.Content,
    private val storageManager: StorageManager,
    private val logging: Logging,
) : ContentInitializer {

    private suspend fun getInitialContentArchive(): Content {
        val initialContentFile = storageManager.getAssetAsFile("content/${runConfigContent.fileName}")
        val archive = ContentArchive(initialContentFile, runConfigContent.secret)
        return Content(
            archive,
            runConfigContent.version,
            runConfigContent.schema,
            runConfigContent.project,
            OTAContent.Type.Release,
        )
    }

    private suspend fun processAndApplyInitialContent(): Content =
        getInitialContentArchive()
            .let { manager.process(it) }
            .let { manager.apply(it) }

    override suspend fun initialize(): Content {
        if (manager.currentContent != null) {
            throw ContentException.InitializerProcessException(
                IllegalStateException("ContentInitializer.initialize() should never be called after a first initialization")
            ).also { logging.e(it.toString()) }
        }

        val forcedContent: Content? = manager.forcedContent
        val contentToApply = manager.contentToApply
        val contentToApplyVersion = contentToApply?.version
        val initialContentVersion = runConfigContent.version
        storageManager.projectTag = runConfigContent.project
        val isNewVersionOrNewProject =
            if (contentToApplyVersion != null && contentToApply.project == runConfigContent.project) {
                contentToApplyVersion >= initialContentVersion
            } else {
                true
            }

        return if (forcedContent != null && forcedContent.schema == runConfigContent.schema) {
            try {
                manager.apply(forcedContent, forceApply = true)
            } catch (t: Throwable) {
                logging.d("Applying initial content since forcedContent failed", throwable = t)
                processAndApplyInitialContent()
            }
        } else if (
            contentToApply != null
            && contentToApply.project !in runConfigContent.deprecatedProjects
            && isNewVersionOrNewProject
        ) {
            try {
                manager.apply(contentToApply)
            } catch (t: Throwable) {
                logging.d("Applying initial content since new Content failed", throwable = t)
                processAndApplyInitialContent()
            }
        } else {
            processAndApplyInitialContent()
        }
    }
}
