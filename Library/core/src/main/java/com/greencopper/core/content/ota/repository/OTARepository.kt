package com.greencopper.core.content.ota.repository

import com.greencopper.core.content.ota.OTAContent
import java.io.File
import java.time.Duration

public interface OTARepository {

    /** Retrieve all the [OTAContent] available */
    public suspend fun getContents(otaApiUrl: String, fetchTimeout: Duration? = null): List<OTAContent>

    /** Retrieve the archive related to this [OTAContent] */
    public suspend fun getArchiveFile(otaContent: OTAContent, downloadTimeout: Duration? = null): File
}
