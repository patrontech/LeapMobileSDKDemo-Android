package com.greencopper.core.content.archive

import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class ConcreteContentArchiveOpenerTest {

    private val storageManager: StorageManager
    private val archiveOpener: ContentArchiveOpener

    init {
        Toolkit.setupTest()
        archiveOpener = ConcreteContentArchiveOpener(App.resolve(), App.resolve())
        storageManager = App.resolve()
    }

    @Test
    fun whenOpeningArchive_withoutCorrectStructure_shouldThrow() {
        runTest {
            val testDirectory = File(storageManager.getCacheStorage(), "archive_opener_tests1")
            val archive = ContentArchive(
                storageManager.getAssetAsFile("content/content_malformed_version.zip"),
                "UT_Salt"
            )

            val destination = testDirectory.apply { mkdirs() }
            assertThrows<ArchiveOpenerException.MalformedArchiveException> {
                archiveOpener.open(archive, 1, 1, destination)
            }
        }
    }

    @Test
    fun whenOpeningArchive_withFakePath_shouldThrow() {
        runTest {
            val testDirectory = File(storageManager.getCacheStorage(), "archive_opener_tests2")
            val archive = ContentArchive(
                storageManager.getAssetAsFile("content/fake_zip.zip"),
                "UT_Salt"
            )
            val destination = testDirectory.apply { mkdirs() }
            assertThrows<ArchiveOpenerException.OpenArchiveException> {
                archiveOpener.open(archive, 1, 1, destination)
            }
        }
    }

    @Test
    fun whenOpeningArchive_withWrongVersion_shouldThrow() {
        runTest {
            val testDirectory = File(storageManager.getCacheStorage(), "archive_opener_tests3")
            val archive = ContentArchive(
                storageManager.getAssetAsFile("content/content_light.zip"),
                "UT_Salt"
            )
            val destination = testDirectory.apply { mkdirs() }
            assertThrows<ArchiveOpenerException.WrongVersionException> {
                archiveOpener.open(archive, 3, 1, destination)
            }
        }
    }

    @Test
    fun whenOpeningArchive_withWrongSchema_shouldThrow() {
        runTest {
            val testDirectory = File(storageManager.getCacheStorage(), "archive_opener_tests4")
            val archive = ContentArchive(
                storageManager.getAssetAsFile("content/content_light.zip"),
                "UT_Salt"
            )
            val destination = testDirectory.apply { mkdirs() }
            assertThrows<ArchiveOpenerException.WrongSchemaException> {
                archiveOpener.open(archive, 1, 2, destination)
            }
        }
    }

    @Test
    fun whenOpeningArchive_withExistingDestination_shouldThrow() {
        runTest {
            val testDirectory = File(storageManager.getCacheStorage(), "archive_opener_tests5")
            val archive = ContentArchive(
                storageManager.getAssetAsFile("content/content_light.zip"),
                "UT_Salt"
            )
            val destination = testDirectory.apply {
                deleteRecursively()
                createNewFile()
                writeText("This is a file!")
            }
            assertThrows<ArchiveOpenerException.MoveArchiveException> {
                archiveOpener.open(archive, 1, 1, destination)
            }
            destination.delete()
        }
    }

    @Test
    fun whenOpeningArchive_withReadOnlyDestination_shouldThrow() {
        runTest {
            val testDirectory = File(storageManager.getCacheStorage(), "archive_opener_tests6")
            val archive = ContentArchive(
                storageManager.getAssetAsFile("content/content_light.zip"),
                "UT_Salt"
            )
            val destination = testDirectory.apply {
                deleteRecursively()
                mkdirs()
            }
            if (!destination.setReadOnly()) return@runTest // This is a workaround for the test to run on Windows

            assertThrows<ArchiveOpenerException.MoveArchiveException> {
                archiveOpener.open(archive, 1, 1, destination)
            }
            destination.deleteRecursively()
        }
    }

    @Test
    fun whenOpeningArchive_shouldHaveConfigurationFiles() {
        runTest {
            val testDirectory = File(storageManager.getCacheStorage(), "archive_opener_tests7")
            val contentConfiguration = RunConfiguration.Companion.build(storageManager, App.resolve()).content
            val archive = ContentArchive(
                storageManager.getAssetAsFile("content/${contentConfiguration.fileName}"),
                contentConfiguration.secret
            )
            val destination = testDirectory.apply {
                mkdirs()
            }
            val archiveOpener = ConcreteContentArchiveOpener(App.resolve(), App.resolve())

            val result = archiveOpener.open(
                archive,
                contentConfiguration.version,
                contentConfiguration.schema,
                destination
            )

            assertThat(result.list()).contains("core")
            assertThat(result.list()).contains("version.json")
            val versionFile = File(result, "version.json")
            val versionConfiguration = App.resolve<Json>().decodeFromString<VersionConfiguration>(versionFile.readText())
            assertThat(versionConfiguration).isNotNull

            destination.deleteRecursively()
        }
    }
}
