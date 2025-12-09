package com.greencopper.testmocks.core

import com.greencopper.core.asset.manager.AssetsManager
import com.greencopper.core.asset.recipe.Asset
import java.io.File
import java.io.InputStream

public class MockAssetsManager(
    public var _missingAssets: () -> Unit = {},
    public var _unusedAssets: () -> Unit = {},
    public var _downloadAssetBlock: (Asset) -> File = { _ -> File("") },
    public var _assetsFromConfig: () -> Set<Asset> = { emptySet() },
    public var _availableAssets: () -> Set<Asset> = { emptySet() },
    public var _getAssetInputStream: (Asset) -> InputStream? = { null },
    public var _getAssetFromName: (String?) -> Asset? = { null },
) : AssetsManager {
    override suspend fun loadMissingAssets(): Unit =
        _missingAssets()

    override suspend fun cleanUnusedAssets(): Unit =
        _unusedAssets()

    override suspend fun downloadAsset(asset: Asset): File =
        _downloadAssetBlock(asset)

    override fun assetsFromConfig(): Set<Asset> =
        _assetsFromConfig()

    override suspend fun availableAssets(): Set<Asset> =
        _availableAssets()

    override suspend fun getAssetInputStream(asset: Asset): InputStream? =
        _getAssetInputStream(asset)

    override fun getAssetFromName(imageName: String?): Asset? =
        _getAssetFromName(imageName)
}
