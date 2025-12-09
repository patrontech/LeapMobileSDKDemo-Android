package com.greencopper.toolkit.storage

import android.content.Context
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.runBlocking
import java.io.File

internal class InternalStorageManager(private val context: Context, private val logger: Logging) : StorageManager {

    override var projectTag: String = StorageManager.defaultProjectTag

    override suspend fun getProjectFilesStorage(projectTag: String?): File =
        File(getFilesStorage(), projectTag ?: this.projectTag).apply { mkdir() }

    override suspend fun getFilesStorage(): File = context.filesDir

    override suspend fun getProjectCacheStorage(projectTag: String?): File =
        File(getCacheStorage(), projectTag ?: this.projectTag).apply { mkdir() }

    override suspend fun getCacheStorage(): File = context.cacheDir

    override suspend fun getAssetAsFile(assetPath: String): File {
        val tempDir = getCacheStorage()
        val file = File(tempDir, assetPath).apply {
            parentFile?.mkdirs()
        }
        try {
            context.assets.open(assetPath).use {
                file.writeBytes(it.readBytes())
            }
        } catch (error: Throwable) {
            logger.e(message = "Couldn't open the asset: $error", throwable = error)
        }
        return file
    }

    override suspend fun deleteFile(file: File): Unit = runBlocking {
        file.deleteRecursively()
    }
}
