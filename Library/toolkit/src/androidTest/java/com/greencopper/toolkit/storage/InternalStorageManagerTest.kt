package com.greencopper.toolkit.storage

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.testmocks.toolkit.MockLogging
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File

internal class InternalStorageManagerTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var storageManager: InternalStorageManager

    @BeforeEach
    fun setup() {
        storageManager = InternalStorageManager(context, MockLogging())
        storageManager.projectTag = StorageManager.defaultProjectTag
    }

    @AfterEach
    fun tearDownEach() {
        runTest {
            storageManager.getCacheStorage().deleteRecursively()
            storageManager.getFilesStorage().deleteRecursively()
        }
    }

    @Test
    fun getProjectFilesStorage() {
        runTest {
            val defaultProjectTag = storageManager.projectTag
            val defaultProjectFile =
                File(storageManager.getProjectFilesStorage(), "projectFileStorageTest")
            val testFileText = "Hello I'm the first test"
            defaultProjectFile.writeText(testFileText)
            assertThat(defaultProjectFile).hasContent(testFileText)

            storageManager.projectTag = "newProject"
            val newProjectFile = File(storageManager.getProjectFilesStorage(), "projectFileStorageTest")
            assertThat(newProjectFile).doesNotExist()

            val explicitDefaultProjectTestFile =
                File(storageManager.getProjectFilesStorage(defaultProjectTag), "projectFileStorageTest")
            assertThat(explicitDefaultProjectTestFile).hasContent(testFileText)
        }
    }

    @Test
    fun getFilesStorage() {
        runTest {
            val permanentProjectStorageFile =
                File(storageManager.getProjectFilesStorage(), "fileStorageTest")
            val testFileText = "Hello I'm the first test"
            permanentProjectStorageFile.writeText(testFileText)
            assertThat(permanentProjectStorageFile).hasContent(testFileText)

            val permanentStorageFile = File(storageManager.getFilesStorage(), "fileStorageTest")
            assertThat(permanentStorageFile).doesNotExist()

            assertThat(permanentProjectStorageFile).hasContent(testFileText)
        }
    }

    @Test
    fun getProjectCacheStorage() {
        runTest {
            val defaultProjectTag = storageManager.projectTag
            val projectCacheStorageFile = File(storageManager.getProjectCacheStorage(), "testFile")
            val testFileText = "Hello I'm the first test"
            projectCacheStorageFile.writeText(testFileText)
            assertThat(projectCacheStorageFile).hasContent(testFileText)

            storageManager.projectTag = "newProject"
            val newProjectCacheStorageFile = File(storageManager.getProjectCacheStorage(), "testFile")
            assertThat(newProjectCacheStorageFile).doesNotExist()

            val explicitProjectCacheStorageFile =
                File(storageManager.getProjectCacheStorage(defaultProjectTag), "testFile")
            assertThat(explicitProjectCacheStorageFile).hasContent(testFileText)
        }
    }

    @Test
    fun getCacheStorage() {
        runTest {
            val projectCacheStorageFile = File(storageManager.getProjectCacheStorage(), "testFile")
            val testFileTest = "Hello I'm the first test"
            projectCacheStorageFile.writeText(testFileTest)
            assertThat(projectCacheStorageFile).hasContent(testFileTest)

            val cacheStorageFile = File(storageManager.getCacheStorage(), "testFile")
            assertThat(cacheStorageFile).doesNotExist()

            assertThat(projectCacheStorageFile).hasContent(testFileTest)
        }
    }

    @Test
    fun getAssetAsFile() {
        runTest {
            val assetTestFile = storageManager.getAssetAsFile("assetTestFile.txt")
            assertThat(assetTestFile).exists()
            assertThat(assetTestFile).hasContent("Hello, I'm an asset")
            val fakeAssetTestFile = storageManager.getAssetAsFile("doesNotExist.txt")
            assertThat(fakeAssetTestFile).doesNotExist()
            val subDirTestFile = storageManager.getAssetAsFile("subDir/subDirAssetFile.txt")
            assertThat(subDirTestFile).exists()
            assertThat(subDirTestFile).hasContent("Hello, I'm an asset in a directory")
        }
    }

    @Test
    fun deleteFileShouldSucceed() {
        runTest {
            val fileToDelete = File(storageManager.getFilesStorage(), "file_to_delete")
            fileToDelete.createNewFile()
            assertThat(fileToDelete).exists()
            storageManager.deleteFile(fileToDelete)
            assertThat(fileToDelete).doesNotExist()
        }
    }
}
