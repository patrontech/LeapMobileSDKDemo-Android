package com.greencopper.testmocks.core

import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.ota.repository.OTARepository
import com.greencopper.toolkit.testing.unimplemented
import java.io.File
import java.time.Duration

public class MockOTARepository(
    public var getContentsValue: () -> List<OTAContent> = { unimplemented() },
    public var getArchiveFileValue: () -> File = { unimplemented() },
) : OTARepository {

    override suspend fun getContents(otaApiUrl: String, fetchTimeout: Duration?): List<OTAContent> =
        getContentsValue()

    override suspend fun getArchiveFile(otaContent: OTAContent, downloadTimeout: Duration?): File =
        getArchiveFileValue()
}
