package com.greencopper.testmocks.core

import android.content.res.AssetManager
import com.greencopper.core.asset.manager.AssetsStorageManager
import java.io.File

public class MockAssetsStorageManager(
    imagesList: List<String>,
    public var downloadDirectory: File,
    public var assetsDirectoryRelativePath: String = "testContent",
    public val androidAssetManager: AssetManager,
) : AssetsStorageManager {

    public var removeAssetsCount: Int = 0
        private set
    public val localImages: ArrayList<String> = ArrayList(imagesList)

    override fun getRelativeAssetsDirectoryPath(): String = assetsDirectoryRelativePath

    override fun getAssetManager(): AssetManager = androidAssetManager

    override suspend fun getAssetsDownloadDirectory(): File =
        downloadDirectory

    override suspend fun removeAssets(fileNames: Set<String>) {
        localImages.removeAll(fileNames).also { removeAssetsCount++ }
    }
}

