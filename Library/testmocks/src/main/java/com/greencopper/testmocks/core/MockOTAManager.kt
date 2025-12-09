package com.greencopper.testmocks.core

import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.ota.OTAManager
import com.greencopper.toolkit.testing.unimplemented
import java.time.Duration

public class MockOTAManager(
    public var availableOTAContentsValue: () -> List<OTAContent> = { unimplemented() },
    public var otaContentToProcessValue: () -> OTAContent? = { unimplemented() },
    public var processValue: () -> Content = { unimplemented() },
    public var forceValue: () -> Content = { unimplemented() },
) : OTAManager {

    public var availableOTAContentsCount: Int = 0
    public var otaContentToProcessCount: Int = 0
    public var processCount: Int = 0

    override suspend fun availableOTAContents(fetchTimeout: Duration?): List<OTAContent> =
        availableOTAContentsValue().also { availableOTAContentsCount++ }

    override suspend fun availableOTAContents(
        otaApiUrl: String,
        fetchTimeout: Duration?
    ): List<OTAContent> =
        availableOTAContentsValue().also { availableOTAContentsCount++ }

    override suspend fun otaContentToProcess(fetchTimeout: Duration?): OTAContent? =
        otaContentToProcessValue().also {
            otaContentToProcessCount++
        }

    override suspend fun otaContentToProcess(
        otaApiUrl: String,
        fetchTimeout: Duration?
    ): OTAContent? =
        otaContentToProcessValue().also { otaContentToProcessCount++ }

    override suspend fun process(
        otaContent: OTAContent,
        saveInHistory: Boolean,
        downloadTimeout: Duration?
    ): Content =
        processValue().also { processCount++ }

    override suspend fun force(otaContent: OTAContent, downloadTimeout: Duration?): Content =
        forceValue()
}
