package com.greencopper.core.asset.manager

import android.content.Context
import android.content.res.AssetManager
import com.greencopper.core.asset.recipe.AssetsConfiguration
import com.greencopper.core.asset.recipe.AssetsConfigurationHolder
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.d
import com.greencopper.toolkit.storage.StorageManager
import java.io.File

public interface AssetsStorageManager {

    /** Get assets' directory relative to the Android's assets root */
    public fun getRelativeAssetsDirectoryPath(): String

    /** Get asset manager */
    public fun getAssetManager(): AssetManager

    /** Get assets download directory */
    public suspend fun getAssetsDownloadDirectory(): File

    /** Removes files listed if possible */
    public suspend fun removeAssets(fileNames: Set<String>)
}

internal class ConcreteAssetsStorageManager(
    private val context: Context,
    private val assetsConfigurationHolder: AssetsConfigurationHolder,
    private val storageManager: StorageManager,
    private val logger: Logging,
) : AssetsStorageManager {
    private val assetsConfiguration: AssetsConfiguration
        get() = assetsConfigurationHolder.currentConfiguration.value
            ?: throw AssetsManagerException.NoConfigurationException()

    override fun getRelativeAssetsDirectoryPath(): String = "content"

    override fun getAssetManager(): AssetManager = context.assets

    override suspend fun getAssetsDownloadDirectory(): File =
        File(
            storageManager.getProjectFilesStorage(assetsConfiguration.project),
            "assets"
        ).apply { mkdirs() }

    override suspend fun removeAssets(fileNames: Set<String>) {
        getAssetsDownloadDirectory().listFiles()
            ?.forEach {
                if (it.name in fileNames) {
                    logger.d("Cleaned ${it.name}")
                    it.delete()
                }
            }
    }
}
