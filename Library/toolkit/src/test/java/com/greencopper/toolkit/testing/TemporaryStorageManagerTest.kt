package com.greencopper.toolkit.testing

import com.greencopper.testmocks.CoroutineTest
import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

internal class TemporaryStorageManagerTest : CoroutineTest(StandardTestDispatcher()) {
    private val storageManager = TemporaryStorageManager()

    init {
        storageManager.projectTag = StorageManager.defaultProjectTag
        runTest {
            storageManager.getCacheStorage().deleteRecursively()
            storageManager.getFilesStorage().deleteRecursively()
        }
    }

    override fun afterEach() {
        storageManager.projectTag = StorageManager.defaultProjectTag
        runTest {
            storageManager.getCacheStorage().deleteRecursively()
            storageManager.getFilesStorage().deleteRecursively()
        }
    }

    @Test
    fun getProjectFilesStorage() {
        testScope.launch {
            val defaultProjectTag = storageManager.projectTag
            val defaultProjectFile = File(storageManager.getProjectFilesStorage(), "projectFileStorageTest")
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
        testScope.launch {
            val permanentProjectStorageFile = File(storageManager.getProjectFilesStorage(), "fileStorageTest")
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
        testScope.launch(Dispatchers.IO) {
            val defaultProjectTag = storageManager.projectTag
            val projectCacheStorageFile = File(storageManager.getProjectCacheStorage(), "testFile")
            val testFileText = "Hello I'm the first test"
            projectCacheStorageFile.writeText(testFileText)
            assertThat(projectCacheStorageFile).hasContent(testFileText)

            storageManager.projectTag = "newProject"
            val newProjectCacheStorageFile = File(storageManager.getProjectCacheStorage(), "testFile")
            assertThat(newProjectCacheStorageFile).doesNotExist()

            val explicitProjectCacheStorageFile = File(storageManager.getProjectCacheStorage(defaultProjectTag), "testFile")
            assertThat(explicitProjectCacheStorageFile).hasContent(testFileText)
        }
    }

    @Test
    fun getCacheStorage() {
        testScope.launch {
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
        testScope.launch {
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
    fun deleteFile() {
        testScope.launch {
            val testFile = File(storageManager.getFilesStorage(), "testFileToDelete.txt")
            testFile.writeText("This file will be deleted")
            assertThat(testFile).exists()

            storageManager.deleteFile(testFile)
            assertThat(testFile).doesNotExist()
        }
    }
}
