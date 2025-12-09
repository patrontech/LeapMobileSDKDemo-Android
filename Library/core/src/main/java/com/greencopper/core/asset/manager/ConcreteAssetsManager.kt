package com.greencopper.core.asset.manager

import android.webkit.URLUtil
import com.greencopper.core.asset.recipe.*
import com.greencopper.core.networking.CoreAPI
import com.greencopper.toolkit.httpclient.saveToFile
import com.greencopper.toolkit.logging.*
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.*

public interface AssetsManager {
    /** Load the missing assets asynchronously */
    public suspend fun loadMissingAssets()

    /** Remove unused assets relative to the current version */
    public suspend fun cleanUnusedAssets()

    /** Download a single image */
    public suspend fun downloadAsset(asset: Asset): File
    public fun assetsFromConfig(): Set<Asset>
    public suspend fun availableAssets(): Set<Asset>

    /** Get InputStream of Asset */
    public suspend fun getAssetInputStream(asset: Asset): InputStream?

    public fun getAssetFromName(imageName: String?): Asset?
}

internal class ConcreteAssetsManager(
    private val coreAPI: CoreAPI,
    private val assetsStorageManager: AssetsStorageManager,
    private val assetsConfigurationHolder: AssetsConfigurationHolder,
    private val logger: Logging,
    private val coroutineScope: CoroutineScope,
) : AssetsManager {

    private val assetsConfiguration: AssetsConfiguration
        get() = assetsConfigurationHolder.currentConfiguration.value
            ?: throw AssetsManagerException.NoConfigurationException()

    override suspend fun loadMissingAssets() {
        val localAssets = bundledAssets() + downloadedAssets()
        val assetsToDownload = assetsFromConfig()
            .filterNot { localAssets.contains(it.name) || it.onDemandOnly == true }
            .sortedWith(compareByDescending(nullsFirst()) { it.priority })
            .toSet()
        downloadAssets(
            assetsToDownload,
            assetsStorageManager.getAssetsDownloadDirectory()
        )
    }

    override suspend fun downloadAsset(
        asset: Asset,
    ): File = withContext(coroutineScope.coroutineContext) {
        if (URLUtil.isValidUrl(asset.url)) {
            download(
                asset.url,
                assetsStorageManager.getAssetsDownloadDirectory()
            ).also {
                logger.i(message = "Success download asset: $asset")
            }
        } else {
            throw NoSuchFileException(
                reason = "URL ${asset.url} is invalid",
                file = File(asset.name)
            )
        }
    }

    override suspend fun cleanUnusedAssets() {
        val namesToRemove = downloadedAssets()
            .minus(assetsConfiguration.assets.map { it.name }.toSet())
            .minus(bundledAssets())
        assetsStorageManager.removeAssets(namesToRemove)
    }

    private suspend fun downloadedAssets(): Set<String> =
        assetsStorageManager.getAssetsDownloadDirectory().list()?.toSet()
            ?: emptySet()

    private fun bundledAssets(): Set<String> {
        return bundledAssets ?: run {
            (assetsStorageManager.getAssetManager()
                .list(assetsStorageManager.getRelativeAssetsDirectoryPath())
                ?.toSet()
                ?: emptySet()
                    )
                .also {
                    bundledAssets = it
                }
        }
    }

    override fun assetsFromConfig(): Set<Asset> {
        return assetsConfiguration
            .assets
            .toMutableSet()
    }

    override suspend fun availableAssets(): Set<Asset> = withContext(coroutineScope.coroutineContext) {
        val localAssets = downloadedAssets() + bundledAssets()
        val configAssets = assetsFromConfig()

        return@withContext configAssets.filter { localAssets.contains(it.name) }.toSet()
    }

    private fun downloadAssets(
        assetsToDownload: Set<Asset>,
        downloadDirectory: File,
    ) {
        coroutineScope.launch {
            assetsToDownload.forEach {
                try {
                    download(it.url, downloadDirectory)
                } catch (throwable: Throwable) {
                    logDownloadErrors(throwable)
                }
            }
        }
    }

    private fun logDownloadErrors(error: Throwable) {
        val notBadNetworkErrors = error !is IOException && error !is HttpException
        if (notBadNetworkErrors) {
            logger.e(
                "Couldn't download one or more assets",
                throwable = error
            )
        }
    }

    private suspend fun download(url: String, downloadDirectory: File): File =
        coreAPI.downloadFile(url).saveToFile(url, downloadDirectory)

    override fun getAssetFromName(imageName: String?): Asset? = imageName?.let { name ->
        assetsFromConfig().find { it.name == name }
    }

    override suspend fun getAssetInputStream(asset: Asset): InputStream? {
        val imageName = asset.name
        return if (bundledAssets().contains(imageName)) {
            assetsStorageManager.getAssetManager()
                .open("${assetsStorageManager.getRelativeAssetsDirectoryPath()}/$imageName")
        } else if (downloadedAssets().contains(imageName)) {
            val imageFile = File(assetsStorageManager.getAssetsDownloadDirectory(), imageName)
            FileInputStream(imageFile)
        } else null
    }

    companion object {
        var bundledAssets: Set<String>? = null
    }
}
