package com.greencopper.core.content.ota

import com.greencopper.core.content.manager.Content
import java.time.Duration

public interface OTAManager {
    /** Get a list of all the available OTA Contents */
    public suspend fun availableOTAContents(fetchTimeout: Duration? = null): List<OTAContent>

    /** Get a list of all the available OTA Contents from a specific OTA url */
    public suspend fun availableOTAContents(
        otaApiUrl: String,
        fetchTimeout: Duration? = null
    ): List<OTAContent>

    /** Find the eligible content with the highest version */
    public suspend fun otaContentToProcess(fetchTimeout: Duration? = null): OTAContent?

    /** Find the eligible content with the highest version from a specific OTA url */
    public suspend fun otaContentToProcess(
        otaApiUrl: String,
        fetchTimeout: Duration? = null
    ): OTAContent?

    /** Convert and process an [OTAContent] to a [Content]*/
    public suspend fun process(
        otaContent: OTAContent,
        saveInHistory: Boolean = true,
        downloadTimeout: Duration? = null
    ): Content

    public suspend fun force(otaContent: OTAContent, downloadTimeout: Duration? = null): Content
}
