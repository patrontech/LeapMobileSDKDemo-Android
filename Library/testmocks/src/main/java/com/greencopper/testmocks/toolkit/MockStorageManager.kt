package com.greencopper.testmocks.toolkit

import com.greencopper.toolkit.storage.StorageManager
import java.io.File

public class MockStorageManager(
    override var projectTag: String = "project",
    private val projectFilesStorage: (String?) -> File = { File("") },
    private val filesStorage: () -> File = { File("") },
    private val projectCacheStorage: (String?) -> File = { File("") },
    private val cacheStorage: () -> File = { File("") },
    private val assetAsFile: (String) -> File = { File("") },
    private val deleteFileAnswer: (File) -> Unit = { _ -> },
) : StorageManager {
    public var deleteFileCallCount: Int = 0
    override suspend fun getProjectFilesStorage(projectTag: String?): File = projectFilesStorage(projectTag)

    override suspend fun getFilesStorage(): File = filesStorage()

    override suspend fun getProjectCacheStorage(projectTag: String?): File = projectCacheStorage(projectTag)

    override suspend fun getCacheStorage(): File = cacheStorage()

    override suspend fun getAssetAsFile(assetPath: String): File = assetAsFile(assetPath)

    override suspend fun deleteFile(file: File): Unit = deleteFileAnswer(file).also { deleteFileCallCount++ }
}
