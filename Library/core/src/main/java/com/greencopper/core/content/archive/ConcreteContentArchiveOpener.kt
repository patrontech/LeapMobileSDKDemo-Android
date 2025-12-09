package com.greencopper.core.content.archive

import com.greencopper.core.content.manager.ContentSchema
import com.greencopper.core.content.manager.ContentVersion
import com.greencopper.toolkit.extensions.mapErrorNotType
import com.greencopper.toolkit.zip.ZipClient
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

internal class ConcreteContentArchiveOpener(
    private val zipClient: ZipClient,
    private val json: Json,
) : ContentArchiveOpener {

    override suspend fun open(
        archive: ContentArchive,
        expectedVersion: ContentVersion,
        expectedSchema: ContentSchema,
        destination: File
    ): File {
        val password = computePassword(archive)
        val tempDirectory = createTempDirectory()

        val unzippedFile = try {
            zipClient.unZipEncryptedFile(archive.file, tempDirectory, password, false)
        } catch (t: Throwable) {
            throw t.mapErrorNotType<ArchiveOpenerException> { ArchiveOpenerException.OpenArchiveException(archive, t) }
        }

        try {
            checkContentInfo(archive, unzippedFile, expectedVersion, expectedSchema)
        } catch (t : Throwable) {
            throw t.mapErrorNotType<ArchiveOpenerException> { ArchiveOpenerException.MalformedArchiveException(archive, it) }
        }

        return try {
            moveArchive(unzippedFile, destination, archive)
        } catch (t: Throwable) {
            throw t.mapErrorNotType<ArchiveOpenerException> { ArchiveOpenerException.MoveArchiveException(archive, it) }
        }
    }

    private fun moveArchive(
        dataDirectory: File,
        destination: File,
        archive: ContentArchive
    ): File {
        if (!destination.isDirectory && !destination.mkdirs()) {
            throw ArchiveOpenerException.MoveArchiveException(
                archive,
                IOException("Couldn't create destination directory")
            )
        }
        dataDirectory.copyRecursively(destination, overwrite = true)
        dataDirectory.deleteRecursively()
        return destination
    }

    private fun checkContentInfo(
        archive: ContentArchive,
        dataDirectory: File,
        expectedVersion: ContentVersion,
        expectedSchema: ContentSchema
    ) {
        val versionFile = File(dataDirectory, "version.json")
        val versionConfiguration =
            json.decodeFromString(VersionConfiguration.serializer(), versionFile.readText())
        val archiveVersion = versionConfiguration.version
        val archiveSchema = versionConfiguration.schema
        if (archiveVersion != expectedVersion) {
            throw ArchiveOpenerException.WrongVersionException(
                expected = expectedVersion, actual = archiveVersion,
                archive = archive
            )
        }
        if (archiveSchema != expectedSchema) {
            throw ArchiveOpenerException.WrongSchemaException(
                expected = expectedSchema, actual = archiveSchema,
                archive = archive
            )
        }
    }

    private fun createTempDirectory(): File {
        val tempDirectory = File.createTempFile("zip", null)
        tempDirectory.delete()
        tempDirectory.mkdir()
        return tempDirectory
    }

    private fun computePassword(archive: ContentArchive): String =
        archive.file.name.replace(".zip", "${archive.secret}zip")
}
