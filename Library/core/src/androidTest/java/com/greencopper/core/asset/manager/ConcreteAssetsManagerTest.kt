package com.greencopper.core.asset.manager

import android.content.res.AssetManager
import android.webkit.URLUtil
import com.greencopper.core.asset.recipe.*
import com.greencopper.coremocks.MockCoreAPI
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockAssetsStorageManager
import com.greencopper.testmocks.core.MockFiles
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.httpclient.saveToFile
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import retrofit2.Response
import java.io.File
import java.io.FileInputStream

internal class ConcreteAssetsManagerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
        ConcreteAssetsManager.bundledAssets = null
    }

    private val coreAPI = MockCoreAPI()
    private val androidAssetManager = mockk<AssetManager>()
    private val mockAssetsStorageManager =
        MockAssetsStorageManager(
            emptyList(),
            mockk<File>(relaxed = true),
            androidAssetManager = this@ConcreteAssetsManagerTest.androidAssetManager
        )
    private val configHolder = AssetsConfigurationHolder()

    private val assetsManager = ConcreteAssetsManager(
        coreAPI,
        mockAssetsStorageManager,
        configHolder,
        MockLogging(),
        testScope
    )

    override fun afterEach() {}

    @Test
    @DisplayName("Given missing assets, When we call loadMissingAssets, Then no exception is thrown and download is called for every assets")
    fun loadTwoMissingAssetShouldSucceed() {
        val assets = listOf(
            Asset("asset_1.png", "https://www.png.com"),
            Asset("asset_99.png", "https://www.png.com")
        )
        configHolder.currentConfiguration.value = AssetsConfiguration(
            assets = assets,
            imagePlaceholderName = "placeholder",
            failedImageName = "failedImage",
            project = "project"
        )

        every { this@ConcreteAssetsManagerTest.androidAssetManager.list(any()) } returns emptyArray()

        runTest {
            assertDoesNotThrow {
                assetsManager.loadMissingAssets()
            }
            assertThat(coreAPI.downloadFileCount).isEqualTo(assets.size)
        }
    }

    @Test
    @DisplayName("Given a wrong path for assets, When we call loadMissingAssets, Then no exception is thrown and download is called for all assets in priority order")
    fun loadAllMissingAssetShouldSucceed() {
        val assets = listOf(
            Asset("asset_1.png", "https://www.png1.com", priority = 1),
            Asset("asset_non.png", "https://www.pngnone.com"),
            Asset("asset_99.png", "https://www.png99.com", priority = 99)
        )
        mockAssetsStorageManager.assetsDirectoryRelativePath = "wrongPath"
        configHolder.currentConfiguration.value = AssetsConfiguration(
            assets = assets,
            imagePlaceholderName = "placeholder",
            failedImageName = "failedImage",
            project = "project"
        )

        every { this@ConcreteAssetsManagerTest.androidAssetManager.list(any()) } returns emptyArray()

        runTest {
            assertDoesNotThrow {
                assetsManager.loadMissingAssets()
            }
            assertThat(coreAPI.downloadFileCount).isEqualTo(assets.size)
        }
    }

    @Test
    @DisplayName("Given no assets are missing, When we call loadMissingAssets, Then no exception is thrown")
    fun loadMissingAssetShouldReturn() {
        val assetsList = listOf(Asset("asset_1.png", "https://www.png.com"))
        configHolder.currentConfiguration.value = AssetsConfiguration(
            assets = assetsList,
            imagePlaceholderName = "placeholder",
            failedImageName = "failedImage",
            project = "project"
        )

        every { this@ConcreteAssetsManagerTest.androidAssetManager.list(any()) } returns emptyArray()

        runTest {
            assertDoesNotThrow {
                assetsManager.loadMissingAssets()
            }
            assertThat(coreAPI.downloadFileCount).isEqualTo(assetsList.size)
        }
    }

    @Test
    @DisplayName("Given an asset is provided, When we call downloadAsset, Then download is called once")
    fun downloadOnceShouldSucceed() {
        val asset = Asset("asset_1.png", "https://www.png.com")
        coreAPI.downloadFileResponse = { Response.success("".toResponseBody()) }
        configHolder.currentConfiguration.value = AssetsConfiguration(
            assets = listOf(asset),
            imagePlaceholderName = "placeholder",
            failedImageName = "failedImage",
            project = "project"
        )

        mockkStatic(URLUtil::class)
        every { URLUtil.isValidUrl(any()) } returns true

        mockkStatic("com.greencopper.toolkit.httpclient.HTTPUtilsKt")
        every { any<Response<ResponseBody>>().saveToFile(any(), any()) } returns mockk<File>()

        runTest {
            assetsManager.downloadAsset(asset)
            assertThat(coreAPI.downloadFileCount).isEqualTo(1)
        }
    }

    @Test
    @DisplayName("Given asset is provided with malformed URL, When we call downloadAsset, Then download should throw")
    fun downloadWithMalformedURLshouldThrow() {
        val asset = Asset("asset_1.png", "malformedUrl")
        val assets = listOf(
            asset,
            Asset("asset_99.png", "https://www.png.com")
        )
        configHolder.currentConfiguration.value = AssetsConfiguration(
            assets = assets,
            imagePlaceholderName = "placeholder",
            failedImageName = "failedImage",
            project = "project"
        )

        mockkStatic(URLUtil::class)
        every { URLUtil.isValidUrl(any()) } returns false

        runTest {
            assertThrows<NoSuchFileException> {
                assetsManager.downloadAsset(asset)
            }
            assertThat(coreAPI.downloadFileCount).isEqualTo(0)
        }
    }

    @Test
    @DisplayName("Given no asset is provided, When cleanUnusedAssets is called, Then no exception is thrown")
    fun cleanUnusedAssetsShouldSucceed() {
        configHolder.currentConfiguration.value = AssetsConfiguration(
            assets = emptyList(),
            imagePlaceholderName = "placeholder",
            failedImageName = "failedImage",
            project = "project"
        )

        every { this@ConcreteAssetsManagerTest.androidAssetManager.list(any()) } returns emptyArray()

        runTest {
            assertDoesNotThrow {
                assetsManager.cleanUnusedAssets()
            }
        }
    }

    @Test
    @DisplayName("Given specific assets are provided in config, Only those should be available")
    fun availableAssets_inConfig_available_shouldReturnAsset() {
        runTest {
            val asset = Asset("asset_1.png", "https://www.png.com")

            configHolder.currentConfiguration.value = AssetsConfiguration(
                assets = listOf(asset),
                imagePlaceholderName = "placeholder",
                failedImageName = "failedImage",
                project = "project"
            )

            every { this@ConcreteAssetsManagerTest.androidAssetManager.list(any()) } returns arrayOf(asset.name)

            val assetsAvailable = assetsManager.availableAssets()

            // asset_2.png shouldn't be in that list even though it's in the assets
            assertThat(assetsAvailable.size).isEqualTo(1)
            assertThat(assetsAvailable.first().name).isEqualTo(asset.name)
        }
    }

    @Test
    fun availableAssets_inConfig_notAvailable_shouldNotReturnAsset() {
        runTest {
            configHolder.currentConfiguration.value = AssetsConfiguration(
                assets = listOf(
                    Asset("asset_99.png", "https://www.png.com"),
                ),
                imagePlaceholderName = "placeholder",
                failedImageName = "failedImage",
                project = "project"
            )

            every { this@ConcreteAssetsManagerTest.androidAssetManager.list(any()) } returns emptyArray()

            val assetsAvailable = assetsManager.availableAssets()

            assertThat(assetsAvailable).isEmpty()
        }
    }

    @Test
    fun getAssetFromName_withKnownName_shouldReturnAsset() {
        val asset = Asset("asset_99.png", "https://www.png.com")
        configHolder.currentConfiguration.value = AssetsConfiguration(
            assets = listOf(
                asset,
            ),
            imagePlaceholderName = "placeholder",
            failedImageName = "failedImage",
            project = "project"
        )

        val result = assetsManager.getAssetFromName(asset.name)

        assertThat(result).isEqualTo(asset)
    }

    @Test
    fun getAssetFromName_withUnknownName_shouldReturnNull() {
        val asset = Asset("asset_99.png", "https://www.png.com")
        configHolder.currentConfiguration.value = AssetsConfiguration(
            assets = listOf(
                asset,
            ),
            imagePlaceholderName = "placeholder",
            failedImageName = "failedImage",
            project = "project"
        )

        val result = assetsManager.getAssetFromName("test")

        assertThat(result).isNull()
    }

    @Test
    fun getInputStream_withBundledAsset_shouldSucceed() {
        runTest {
            val asset = Asset("asset_1.png", "https://www.png.com")
            configHolder.currentConfiguration.value = AssetsConfiguration(
                assets = listOf(
                    asset,
                ),
                imagePlaceholderName = "placeholder",
                failedImageName = "failedImage",
                project = "project"
            )

            every { this@ConcreteAssetsManagerTest.androidAssetManager.list(any()) } returns arrayOf(asset.name)
            every { this@ConcreteAssetsManagerTest.androidAssetManager.open(any()) } returns mockk<FileInputStream>()

            val result = assetsManager.getAssetInputStream(asset)
            assertThat(result).isNotNull()
        }
    }

    @Test
    fun getInputStream_withDownloadedAsset_shouldSucceed() {
        runTest {
            val asset = Asset("my_schedule.png", "")
            val rootTmp = MockFiles.createTempFileFrom(this, listOf("/mockDownload/${asset.name}"))

            configHolder.currentConfiguration.value = AssetsConfiguration(
                assets = listOf(
                    asset,
                ),
                imagePlaceholderName = "placeholder",
                failedImageName = "failedImage",
                project = "project"
            )
            mockAssetsStorageManager.downloadDirectory = File(rootTmp, "mockDownload")

            every { this@ConcreteAssetsManagerTest.androidAssetManager.list(any()) } returns emptyArray()

            val result = assetsManager.getAssetInputStream(asset)
            assertThat(result).isInstanceOf(FileInputStream::class.java)

            rootTmp.deleteRecursively()
        }
    }

    @Test
    fun getInputStream_withUnknownAsset_shouldSucceed() {
        runTest {
            val asset = Asset("test.png", "")

            configHolder.currentConfiguration.value = AssetsConfiguration(
                assets = listOf(
                    asset,
                ),
                imagePlaceholderName = "placeholder",
                failedImageName = "failedImage",
                project = "project"
            )

            every { this@ConcreteAssetsManagerTest.androidAssetManager.list(any()) } returns emptyArray()

            val result = assetsManager.getAssetInputStream(asset)
            assertThat(result).isNull()
        }
    }
}
