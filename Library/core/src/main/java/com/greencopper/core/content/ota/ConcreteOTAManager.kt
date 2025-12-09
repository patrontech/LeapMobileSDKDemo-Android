package com.greencopper.core.content.ota

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.content.ota.repository.OTARepository
import com.greencopper.core.draftcontent.DraftContentManager
import com.greencopper.toolkit.logging.*
import java.time.Duration

internal class ConcreteOTAManager(
    private val repository: OTARepository,
    private val contentManager: ContentManager,
    private val draftContentManager: DraftContentManager,
    private val contentConfig: RunConfiguration.Content,
    private val otaApiUrl: String,
    private val logging: Logging,
) : OTAManager {

    override suspend fun availableOTAContents(fetchTimeout: Duration?): List<OTAContent> =
        availableOTAContents(otaApiUrl, fetchTimeout)

    override suspend fun availableOTAContents(
        otaApiUrl: String,
        fetchTimeout: Duration?
    ): List<OTAContent> = try {
        repository.getContents(otaApiUrl, fetchTimeout)
            .filter { otaContent -> otaContent.schema == contentConfig.schema }
    } catch (e : Throwable) {
        logging.e(message = "Couldn't retrieve OTA Content", throwable = e)
        emptyList()
    }

    override suspend fun otaContentToProcess(fetchTimeout: Duration?): OTAContent? =
        otaContentToProcess(otaApiUrl, fetchTimeout)

    override suspend fun otaContentToProcess(otaApiUrl: String, fetchTimeout: Duration?): OTAContent? {
        val contentList = availableOTAContents(otaApiUrl, fetchTimeout)

        val includeDraftContent = draftContentManager.passcode != null
        val potentialOtaContent = contentList.filter {
                it.versionType == OTAContent.Type.Release ||
                        (includeDraftContent && it.versionType == OTAContent.Type.Draft)
            }
            .maxByOrNull { it.version }
            ?: return null
        val eligibleContentsToApply =
            contentManager.eligibleContentsToApply(potentialOtaContent.project)

        val eligibleContent =
            eligibleContentsToApply.firstOrNull() ?: return potentialOtaContent

        return if (potentialOtaContent.version > eligibleContent.version) {
            logging.i(message = "OTA Content to process: $potentialOtaContent")
            potentialOtaContent
        } else {
            logging.i(message = "No OTA Content to process")
            null
        }
    }

    override suspend fun process(
        otaContent: OTAContent,
        saveInHistory: Boolean,
        downloadTimeout: Duration?
    ): Content {
        try {
            var content = convert(otaContent, downloadTimeout)
            logging.d(message = "Processing OTA Content: $otaContent")
            content = contentManager.process(content, saveInHistory)
            logging.i(message = "Success processing OTA Content: $otaContent")
            return content
        } catch (t: Throwable) {
            logging.e(message = "Process failed for OTA Content: $otaContent", throwable = t)
            throw t
        }
    }

    override suspend fun force(otaContent: OTAContent, downloadTimeout: Duration?): Content {
        val content = process(otaContent, false, downloadTimeout)
        return contentManager.apply(content, true)
    }

    private suspend fun convert(otaContent: OTAContent, downloadTimeout: Duration?): Content {
        val archiveFile = repository.getArchiveFile(otaContent, downloadTimeout)
        val archive = ContentArchive(archiveFile, contentConfig.secret)

        return Content(
            archive,
            otaContent.version,
            otaContent.schema,
            otaContent.project,
            otaContent.versionType,
        )
    }
}
