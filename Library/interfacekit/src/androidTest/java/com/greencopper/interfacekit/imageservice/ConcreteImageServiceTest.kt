package com.greencopper.interfacekit.imageservice

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.*
import androidx.core.graphics.drawable.toBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.asset.manager.AssetsManagerException
import com.greencopper.core.asset.recipe.*
import com.greencopper.core.asset.recipe.Asset.Format.Name.THUMBNAIL
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.*
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import io.mockk.spyk
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.*

internal class ConcreteImageServiceTest : CoroutineTest() {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context
    private val assetsConfigurationHolder = AssetsConfigurationHolder()
    private val mockAssetsManager = MockAssetsManager(
        _downloadAssetBlock = { _ -> throw RuntimeException() },
        _getAssetInputStream = {
            context.assets.open("testContent/${it.name}")
        },
        _availableAssets = {
            assetsConfigurationHolder.currentConfiguration.value?.assets?.toSet() ?: emptySet()
        },
        _getAssetFromName = { name ->
            assetsConfigurationHolder.currentConfiguration.value?.assets?.find { it.name == name }
        }
    )
    private val mockAssetsStorageManager =
        MockAssetsStorageManager(emptyList(), MockImageDirectory(arrayListOf()), androidAssetManager = context.assets)
    private val localizationService = MockLocalizationService()
    private val buildConfigProvider = MockBuildConfigProvider()

    private val imageService: ConcreteImageService = spyk(
        ConcreteImageService(
            assetsManager = mockAssetsManager,
            context = context,
            assetsConfigurationHolder = assetsConfigurationHolder,
            logger = MockLogging(),
            localizationService = localizationService,
            buildConfigProvider = buildConfigProvider,
            scope = testScope
        )
    )

    private val projectName = "project"
    private val placeHolderName = "placeholder.png"
    private val placeHolderAsset = Asset(placeHolderName, "")
    private val placeHolderDrawable =
        BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/$placeHolderName"))
        )
    private val failedName = "failedImage.png"
    private val failedAsset = Asset(failedName, "")
    private val failedDrawable =
        BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/$failedName"))
        )

    init {
        Toolkit.setupTest(applicationContext = context)
        bindSingleton(assetsConfigurationHolder)

    }

    override fun afterEach() {}

    @Test
    fun whenGettingImageDrawable_withoutConfiguration_shouldThrow() {
        assetsConfigurationHolder.currentConfiguration.value = null
        assertThrows<AssetsManagerException.NoConfigurationException> {
            runTest {
                imageService.getImageDrawable("image").singleOrNull()
            }
        }
    }

    @Test
    fun whenGettingImageDrawable_withNullName_shouldGetPlaceholder() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig()
        runTest {
            val result = imageService.getImageDrawable(null, hideIfUnknown = false).single()
            assertThat(result).isExactlyInstanceOf(ImageResult.UNKNOWN::class.java)
            assertThat(result.drawable?.bytesEqualTo(placeHolderDrawable)).isTrue
        }
    }

    @Test
    fun whenGettingImageDrawable_withNullName_withMissingPlaceholder_shouldReturnNull() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(
            imagePlaceholderName = "doesntExist.png",
            customImagePlaceholderName = "doesntExist.png",
        )

        runTest {
            val result = imageService.getImageDrawable(null, hideIfUnknown = false).single()
            assertThat(result).isExactlyInstanceOf(ImageResult.UNKNOWN::class.java)
            assertThat(result.drawable).isNull()
        }
    }

    @Test
    fun whenGettingImageDrawable_withNullName_placeholderNotAllowed_shouldGetNull() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig()
        runTest {
            val result = imageService.getImageDrawable(null, hideIfUnknown = true).single()

            assertThat(result).isExactlyInstanceOf(ImageResult.UNKNOWN::class.java)
            assertThat(result.drawable).isNull()
        }
    }

    @Test
    fun whenGettingImageDrawable_withFakeName_noPlaceholder_shouldGetNull() {
        val asset = Asset("doesntExist.png", "")
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))
        mockAssetsManager._getAssetFromName = {
            if (it == asset.name) null else Asset("pouet.png", "")
        }
        runTest {
            val result = imageService.getImageDrawable(asset.name, hideIfUnknown = true).single()
            assertThat(result).isExactlyInstanceOf(ImageResult.UNKNOWN::class.java)
            assertThat(result.drawable).isNull()
        }
    }

    @Test
    fun whenGettingImageDrawable_withNonExistingFile_shouldGetPlaceholder() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig()
        runTest {
            val result = imageService.getImageDrawable("invalidImage").single()
            assertThat(result.drawable?.bytesEqualTo(placeHolderDrawable)).isTrue
        }
        runTest {
            val result = imageService.getImageDrawable("invalidImage.png").single()
            assertThat(result.drawable?.bytesEqualTo(placeHolderDrawable)).isTrue
        }
        runTest {
            val result = imageService.getImageDrawable("invalidImage.svg").single()
            assertThat(result.drawable?.toBitmap()?.sameAs(placeHolderDrawable.toBitmap())).isTrue
        }
    }

    @Test
    fun whenGettingImageDrawable_withCorrectName_shouldGetImage() {
        //given
        val asset = Asset("my_schedule_add.png", "")
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))
        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/${asset.name}"))
        )

        runTest {
            //when
            val result = imageService.getImageDrawable(asset.name).single()

            //then
            result.drawable?.toBitmap()?.sameAs(expectedImage.toBitmap())
            assertThat(result).isExactlyInstanceOf(ImageResult.READY::class.java)
            assertThat(result.drawable?.bytesEqualTo(expectedImage)).isTrue
        }
    }

    @Test
    fun whenGettingGif_withCorrectName_shouldGetGif() {
        //given
        val asset = Asset("test.gif", "")
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))
        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/${asset.name}"))
        )

        runTest {
            //when
            val result = imageService.getImageDrawable(asset.name).single()

            //then
            result.drawable?.toBitmap()?.sameAs(expectedImage.toBitmap())
            assertThat(result).isExactlyInstanceOf(ImageResult.READY::class.java)
            assertThat(result.drawable?.bytesEqualTo(expectedImage)).isTrue
        }
    }

    @Test
    fun whenGettingGifAndroidQ_withCorrectName_shouldGetGif() {
        //given
        buildConfigProvider.mockSdkInt = 29
        val asset = Asset("test.gif", "")
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))
        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/${asset.name}"))
        )

        runTest {
            //when
            val result = imageService.getImageDrawable(asset.name).single()

            //then
            result.drawable?.toBitmap()?.sameAs(expectedImage.toBitmap())
            assertThat(result).isExactlyInstanceOf(ImageResult.READY::class.java)
            assertThat(result.drawable?.bytesEqualTo(expectedImage)).isTrue
        }
    }

    @Test
    fun whenGettingImageDrawable_withCorrectName_notAvailableLocally_shouldDownload_shouldGetImage() {
        //given
        val asset = Asset("my_schedule_remove.png", "")
        val rootTmp = MockFiles.createTempFileFrom(this, listOf("/${asset.name}"))
        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(MockFiles.getInputStreamOfResource(this, "/${asset.name}"))
        )
        var isDownloaded = false
        mockAssetsManager._availableAssets = { if (isDownloaded) setOf(asset) else emptySet() }
        mockAssetsManager._downloadAssetBlock = { asset ->
            isDownloaded = true
            File(rootTmp, asset.name)
        }
        mockAssetsManager._getAssetInputStream = {
            FileInputStream(File(rootTmp, asset.name))
        }
        mockAssetsStorageManager.downloadDirectory = rootTmp

        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))

        runTest {
            //when
            val results = imageService.getImageDrawable(asset.name).toList()

            //then
            assertThat(isDownloaded).isTrue
            assertThat(results.size).isEqualTo(2)
            assertThat(results[0]).isExactlyInstanceOf(ImageResult.LOADING::class.java)
            assertThat(results[0].drawable).isNotNull
            assertThat(results[1]).isExactlyInstanceOf(ImageResult.READY::class.java)
            results[1].drawable?.toBitmap()?.sameAs(expectedImage.toBitmap())
            assertThat(results[1].drawable?.bytesEqualTo(expectedImage)).isTrue
        }

        rootTmp.deleteRecursively()
    }

    @Test
    fun whenGettingFormatedImageDrawable_withWrongFormat_shouldGetOriginalImage() {
        //given
        val asset = Asset(
            "my_schedule_empty.png", "", formats = mapOf(
                "testFormat" to Asset.Format(
                    Asset.Format.Point(2, 3),
                    Asset.Format.Size(100, 200)
                )
            )
        )
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))
        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/${asset.name}"))
        )

        runTest {
            //when
            val result = imageService.getImageDrawable(asset.name, formatName = THUMBNAIL).single()

            //then
            assertThat(result.drawable?.bytesEqualTo(expectedImage)).isTrue
        }
    }

    @Test
    fun whenGettingFormatedImageDrawable_withOversizedFormat_shouldGetOriginalImage() {
        //given
        val asset = Asset(
            "my_schedule_empty.png", "", formats = mapOf(
                THUMBNAIL.formatName to Asset.Format(
                    Asset.Format.Point(2, 3),
                    Asset.Format.Size(1000, 2000)
                )
            )
        )
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))
        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/${asset.name}"))
        )

        runTest {
            //when
            val result = imageService.getImageDrawable(asset.name, formatName = THUMBNAIL).single()

            //then
            assertThat(result.drawable?.bytesEqualTo(expectedImage)).isTrue
        }
    }

    @Test
    fun whenGettingFormatedImageDrawable_withCorrectFormat_shouldGetFormattedImage() {
        //given
        val format = Asset.Format(
            Asset.Format.Point(2, 3),
            Asset.Format.Size(100, 200)
        )
        val asset = Asset(
            "my_schedule_empty.png", "", formats = mapOf(
                THUMBNAIL.formatName to format
            )
        )
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))
        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/${asset.name}"))
                .crop(format)
        )

        runTest {
            val single = imageService.getImageDrawable(asset.name, formatName = THUMBNAIL).single()
            val result = single.drawable
            assertThat(result!!.bytesEqualTo(expectedImage)).isTrue
            assertThat(result.intrinsicWidth).isEqualTo(100)
            assertThat(result.intrinsicHeight).isEqualTo(200)
        }
    }

    @Test
    fun whenGettingSvgImageDrawable_withCorrectName_shouldGetImage() {
        val asset = Asset("ticket.svg", "")
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))
        runTest {
            val result = imageService.getImageDrawable(asset.name).single()
            assertThat(result.drawable?.bytesEqualTo(placeHolderDrawable)).isFalse
        }
    }

    @Test
    fun shouldDownloadCustomPlaceholder_ifNotAvailable() {
        val customPlaceholder = Asset("my_schedule_remove.png", "")

        val rootTmp = MockFiles.createTempFileFrom(this, listOf("/${customPlaceholder.name}"))
        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(MockFiles.getInputStreamOfResource(this, "/${customPlaceholder.name}"))
        )
        var isDownloaded = false
        mockAssetsManager._availableAssets = { if (isDownloaded) setOf(customPlaceholder) else emptySet() }
        mockAssetsManager._downloadAssetBlock = { asset ->
            isDownloaded = true
            File(rootTmp, asset.name)
        }
        mockAssetsManager._getAssetInputStream = {
            if (isDownloaded) FileInputStream(File(rootTmp, customPlaceholder.name)) else null
        }
        mockAssetsStorageManager.downloadDirectory = rootTmp

        assetsConfigurationHolder.currentConfiguration.value = makeConfig(
            listOf(placeHolderAsset, customPlaceholder),
            customImagePlaceholderName = customPlaceholder.name
        )

        runTest {
            val result = imageService.getImageDrawable(null, hideIfUnknown = false).single()
            assertThat(isDownloaded).isTrue
            assertThat(result).isExactlyInstanceOf(ImageResult.UNKNOWN::class.java)
            assertThat(result.drawable?.bytesEqualTo(expectedImage)).isTrue
        }
    }

    @Test
    fun whenGettingFailedImage_shouldDownloadIfNotAvailable() {
        val failed = Asset("my_schedule_remove.png", "")
        val asset = Asset("test.png", "")

        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(MockFiles.getInputStreamOfResource(this, "/${failed.name}"))
        )
        val rootTmp = MockFiles.createTempFileFrom(this, listOf("/${failed.name}"))
        var isDownloaded = false
        var downloadCount = 0
        var downloadedAssetName = ""
        mockAssetsManager._availableAssets = { if (isDownloaded) setOf(failed) else setOf(asset) }
        mockAssetsManager._downloadAssetBlock = { asset ->
            isDownloaded = true
            downloadCount++
            downloadedAssetName = asset.name
            File(rootTmp, asset.name)
        }
        mockAssetsManager._getAssetInputStream = {
            if (isDownloaded) FileInputStream(File(rootTmp, failed.name)) else null
        }
        mockAssetsStorageManager.downloadDirectory = rootTmp

        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset, failedAsset))

        runTest {
            val result = imageService.getImageDrawable(
                asset.name
            ).first()

            assertThat(isDownloaded).isTrue
            assertThat(downloadCount).isEqualTo(1)
            assertThat(downloadedAssetName).isEqualTo(failedName)
            assertThat(result).isExactlyInstanceOf(ImageResult.FAILED::class.java)
            assertThat(result.drawable?.bytesEqualTo(expectedImage)).isTrue
        }
    }

    @Test
    fun whenDownloadingImage_withHideLoadingFlag_shouldOnlyGetReady() {
        //given
        val asset = Asset("my_schedule_remove.png", "")
        val rootTmp = MockFiles.createTempFileFrom(this, listOf("/${asset.name}"))
        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(MockFiles.getInputStreamOfResource(this, "/${asset.name}"))
        )
        var isDownloaded = false
        mockAssetsManager._availableAssets = { if (isDownloaded) setOf(asset) else emptySet() }
        mockAssetsManager._downloadAssetBlock = { asset ->
            isDownloaded = true
            File(rootTmp, asset.name)
        }
        mockAssetsManager._getAssetInputStream = {
            FileInputStream(File(rootTmp, asset.name))
        }
        mockAssetsStorageManager.downloadDirectory = rootTmp

        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset))

        runBlocking {
            //when
            val results = mutableListOf<ImageResult>()
            val job = launch {
                imageService.getImageDrawable(asset.name, hideIfLoading = true).collect {
                    results.add(it)
                }
            }

            delay(1000)

            //then
            assertThat(isDownloaded).isTrue
            assertThat(results[0]).isExactlyInstanceOf(ImageResult.LOADING::class.java)
            results[0].drawable shouldBe null
            assertThat(results[1]).isExactlyInstanceOf(ImageResult.READY::class.java)
            results[1].drawable?.toBitmap()?.sameAs(expectedImage.toBitmap())
            assertThat(results[1].drawable?.bytesEqualTo(expectedImage)).isTrue

            job.cancel()
        }

        rootTmp.deleteRecursively()
    }

    @Test
    fun missingFailedImage() {
        val asset = Asset("my_schedule_remove.png", "")
        assetsConfigurationHolder.currentConfiguration.value =
            makeConfig(listOf(asset, Asset("test.png", "")), failedImageName = "test.png")

        runTest {
            val result = imageService.getImageDrawable(
                asset.name
            ).first()

            assertThat(result).isExactlyInstanceOf(ImageResult.FAILED::class.java)
            assertThat(result.drawable).isInstanceOf(GradientDrawable::class.java)
        }
    }

    @Test
    fun whenFailingSvgParsing_shouldReturnFailed() {
        val asset = Asset("fakeSVG.svg", "")
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset, failedAsset))
        runTest {
            val result = imageService.getImageDrawable("fakeSVG.svg", true).single()

            assertThat(result).isExactlyInstanceOf(ImageResult.FAILED::class.java)
            assertThat(result.drawable?.bytesEqualTo(failedDrawable)).isTrue
        }
    }

    @Test
    fun whenDownloadFail_withFailedAvailable_shouldReturnFailed() {
        val asset = Asset("my_schedule_remove.png", "")
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(listOf(asset, failedAsset))

        runTest {
            val result = imageService.getImageDrawable(
                asset.name
            ).first()

            assertThat(result).isExactlyInstanceOf(ImageResult.FAILED::class.java)
            assertThat(result.drawable?.bytesEqualTo(failedDrawable)).isTrue
        }
    }

    @Test
    fun whenGettingImageDrawable_withNullName_shouldGetCustomPlaceholder() {
        val customPlaceholder = Asset("my_schedule_add.png", "")
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(
            listOf(placeHolderAsset, customPlaceholder),
            customImagePlaceholderName = customPlaceholder.name
        )

        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/${customPlaceholder.name}"))
        )

        runTest {
            val result = imageService.getImageDrawable(null, hideIfUnknown = false).single()
            assertThat(result).isExactlyInstanceOf(ImageResult.UNKNOWN::class.java)
            assertThat(result.drawable?.bytesEqualTo(expectedImage)).isTrue
        }
    }

    @Test
    fun whenGettingImageDrawable_withInvalidName_shouldGetCustomPlaceholder() {
        val customPlaceholder = Asset("my_schedule_add.png", "")
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(
            listOf(placeHolderAsset, customPlaceholder),
            customImagePlaceholderName = customPlaceholder.name
        )

        val expectedImage = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/${customPlaceholder.name}"))
        )

        runTest {
            val result = imageService.getImageDrawable("helloWorld.png", hideIfUnknown = false).single()
            assertThat(result).isExactlyInstanceOf(ImageResult.UNKNOWN::class.java)
            assertThat(result.drawable?.bytesEqualTo(expectedImage)).isTrue
        }
    }

    @Test
    fun whenImageIsInConfig_shouldBeKnown() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig()
        mockAssetsManager._assetsFromConfig = {
            setOf(Asset("image1", "urlImage"))
        }

        assertThat(imageService.isImageKnown("image1")).isTrue
    }

    @Test
    fun whenImageIsNotInConfig_shouldNotBeKnown() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig()
        mockAssetsManager._assetsFromConfig = {
            setOf(Asset("image1", "urlImage"))
        }

        assertThat(imageService.isImageKnown("image2")).isFalse
    }

    @Test
    fun whenImageIsDownloaded_shouldReturnTrue() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig()
        mockAssetsManager._availableAssets = {
            setOf(Asset("image1", "urlImage"))
        }

        assertThat(imageService.isImageAvailable("image1")).isTrue
    }

    @Test
    fun whenImageIsNotDownloaded_shouldReturnFalse() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig()
        mockAssetsManager._availableAssets = {
            setOf(Asset("image1", "urlImage"))
        }

        assertThat(imageService.isImageAvailable("image2")).isFalse
    }

    @Test
    @DisplayName("getImageRatio with unknown image name should return null")
    fun getImageRatioWithUnknownImage() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig()
        val result = imageService.getImageRatio("unknown")
        result shouldBe null
    }

    @Test
    @DisplayName("getImageRatio with known asset and format should return format's ratio")
    fun getImageRatioWithKnownAssetAndFormat() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(
            assets = listOf(
                Asset(
                    name = "image1",
                    url = "urlImage",
                    ratio = 1.67f,
                    formats = mapOf(
                        THUMBNAIL.formatName to Asset.Format(
                            Asset.Format.Point(1, 2),
                            Asset.Format.Size(4, 2),
                        )
                    )
                )
            )
        )

        val result = imageService.getImageRatio("image1", THUMBNAIL)
        result shouldBe 2f
    }

    @Test
    @DisplayName("getImageRatio with known asset and unknown format should return asset's ratio")
    fun getImageRatioWithKnownAssetAndUnkownFormat() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(
            assets = listOf(
                Asset(
                    name = "image1",
                    url = "urlImage",
                    ratio = 1.67f,
                    formats = mapOf(
                        "test" to Asset.Format(
                            Asset.Format.Point(1, 2),
                            Asset.Format.Size(4, 2),
                        )
                    )
                )
            )
        )

        val result = imageService.getImageRatio("image1", THUMBNAIL)
        result shouldBe 1.67f
    }

    @Test
    @DisplayName("getImageRatio with known asset without format should return asset's ratio")
    fun getImageRatioWithKnownAssetWithoutFormat() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(
            assets = listOf(
                Asset(
                    name = "image1",
                    url = "urlImage",
                    ratio = 1.67f,
                    formats = null
                )
            )
        )

        val result = imageService.getImageRatio("image1", THUMBNAIL)
        result shouldBe 1.67f
    }

    @Test
    @DisplayName("getImageRatio with known asset with no given format should return asset's ratio")
    fun getImageRatioWithKnownAssetJustByName() {
        assetsConfigurationHolder.currentConfiguration.value = makeConfig(
            assets = listOf(
                Asset(
                    name = "image1",
                    url = "urlImage",
                    ratio = 1.67f,
                    formats = mapOf(
                        THUMBNAIL.formatName to Asset.Format(
                            Asset.Format.Point(1, 2),
                            Asset.Format.Size(4, 2),
                        )
                    )
                )
            )
        )

        val result = imageService.getImageRatio("image1", null)
        result shouldBe 1.67f
    }

    @Test
    fun givenBitmap_whenRotate_shouldReturnRotatedBitmap() {
        val originalBitmap = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)
        val rotatedBitmap = originalBitmap.rotate(90f)

        assertEquals(50, rotatedBitmap.width)
        assertEquals(100, rotatedBitmap.height)
    }

    private fun makeConfig(
        assets: List<Asset> = listOf(placeHolderAsset, failedAsset),
        imagePlaceholderName: String = placeHolderName,
        customImagePlaceholderName: String? = null,
        failedImageName: String = failedName,
        project: String = projectName,
    ) = AssetsConfiguration(
        assets = assets,
        imagePlaceholderName = imagePlaceholderName,
        customImagePlaceholderName = customImagePlaceholderName,
        failedImageName = failedImageName,
        project = project
    )
}

private fun <T : Drawable> T.bytesEqualTo(t: T?): Boolean =
    toBitmap().bytesEqualTo(t?.toBitmap())

private fun Bitmap.bytesEqualTo(otherBitmap: Bitmap?) =
    otherBitmap?.let { other ->
        if (width == other.width && height == other.height) {
            toBytes().contentEquals(other.toBytes())
        } else false
    } ?: kotlin.run { false }

private fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().use { stream ->
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
    stream.toByteArray()
}
