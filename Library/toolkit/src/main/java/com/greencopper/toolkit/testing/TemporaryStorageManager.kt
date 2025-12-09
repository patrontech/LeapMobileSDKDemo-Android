package com.greencopper.toolkit.testing

import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

public class TemporaryStorageManager : StorageManager {
    override var projectTag: String = StorageManager.defaultProjectTag

    private suspend fun getTemporaryFolderWithName(name: String): File = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile(name + "folder", null)
        val parentTempFolder = tempFile.parentFile
        File(parentTempFolder, name).apply { mkdirs() }
    }

    override suspend fun getProjectFilesStorage(projectTag: String?): File = withContext(Dispatchers.IO) {
        getTemporaryFolderWithName(
            (projectTag ?: this@TemporaryStorageManager.projectTag) + "/projectFilesStorage"
        ).apply {
            mkdirs()
        }
    }

    override suspend fun getFilesStorage(): File = withContext(Dispatchers.IO) {
        getTemporaryFolderWithName("filesStorage")
    }

    override suspend fun getProjectCacheStorage(projectTag: String?): File = withContext(Dispatchers.IO) {
        getTemporaryFolderWithName(
            (projectTag ?: this@TemporaryStorageManager.projectTag) + "/projectCacheStorage"
        ).apply {
            mkdirs()
        }
    }

    override suspend fun getCacheStorage(): File = withContext(Dispatchers.IO) {
        getTemporaryFolderWithName("cacheStorage")
    }

    override suspend fun getAssetAsFile(assetPath: String): File {
        return File("src/test/res", assetPath)
    }

    override suspend fun deleteFile(file: File): Unit = withContext(Dispatchers.IO) {
        file.deleteRecursively()
    }
}
