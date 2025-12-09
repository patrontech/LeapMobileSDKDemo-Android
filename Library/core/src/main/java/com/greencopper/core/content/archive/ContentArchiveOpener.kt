package com.greencopper.core.content.archive

import com.greencopper.core.content.manager.ContentSchema
import com.greencopper.core.content.manager.ContentVersion
import java.io.File

internal interface ContentArchiveOpener {

    /** Unzip, check and move [ContentArchive] to [destination] */
    suspend fun open(
        archive: ContentArchive,
        expectedVersion: ContentVersion,
        expectedSchema: ContentSchema,
        destination: File
    ): File
}
