package com.greencopper.interfacekit.imageservice

import android.content.Context
import android.graphics.*
import android.graphics.drawable.*
import android.media.ThumbnailUtils
import android.os.Build
import androidx.core.content.ContextCompat
import com.greencopper.core.asset.manager.AssetsManager
import com.greencopper.core.asset.manager.AssetsManagerException
import com.greencopper.core.asset.recipe.Asset
import com.greencopper.core.asset.recipe.Asset.Format
import com.greencopper.core.asset.recipe.AssetsConfiguration
import com.greencopper.core.asset.recipe.AssetsConfigurationHolder
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.imageservice.ImageResult.*
import com.greencopper.interfacekit.ui.utils.shimmerdrawable.Shimmer
import com.greencopper.interfacekit.ui.utils.shimmerdrawable.ShimmerDrawable
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import com.pixplicity.sharp.Sharp.loadInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer

public interface ImageService {

    /** Retrieve a flow of [ImageResult] containing information about the current state of image retrieving
     *   @param hideIfUnknown Doesn't try to return a placeholder if image [name] is not known
     *   @param hideIfLoading Doesn't show [ImageResult.LOADING] result
     */
    public fun getImageDrawable(
        name: String?,
        hideIfUnknown: Boolean = false,
        hideIfLoading: Boolean = false,
        formatName: Format.Name? = null,
    ): Flow<ImageResult>

    public fun getImageRatio(name: String, formatName: Format.Name? = null): Float?

    /**
     *  Check if image is known in the recipe
     */
    public fun isImageKnown(name: String): Boolean

    /**
     *  Check if image is downloaded
     */
    public fun isImageAvailable(name: String): Boolean
}

internal class ConcreteImageService(
    private val assetsManager: AssetsManager,
    private val context: Context,
    private val assetsConfigurationHolder: AssetsConfigurationHolder,
    private val logger: Logging,
    private val localizationService: LocalizationService,
    private val buildConfigProvider: BuildConfigProvider,
    private val scope: CoroutineScope,
) : ImageService {

    private val resources = context.resources

    private val assetsConfiguration: AssetsConfiguration
        get() = assetsConfigurationHolder.currentConfiguration.value
            ?: throw AssetsManagerException.NoConfigurationException()

    private fun getAssetFromName(imageName: String?): Asset? {
        val translatedImageName = localizationService.getString(imageName)
        return assetsManager.getAssetFromName(translatedImageName)
    }

    private suspend fun getPlaceholderDrawable(): Drawable? {
        return getAssetFromName(assetsConfiguration.customImagePlaceholderName)?.let {
            getImageDrawableFromAsset(it) ?: run {
                if (downloadAsset(it)) {
                    getImageDrawableFromAsset(it)
                } else null
            }
        } ?: getAssetFromName(assetsConfiguration.imagePlaceholderName)?.let {
            getImageDrawableFromAsset(it)
        } ?: run {
            logger.e(
                message = "Couldn't load placeholder",
                throwable = AssetsManagerException.NoPlaceHolderException()
            )
            null
        }
    }

    private suspend fun getFailedImage(ratioOfOriginalAsset: Float): Drawable? {
        return getAssetFromName(assetsConfiguration.failedImageName)?.let {
            getImageDrawableFromAsset(it, forceCropRatio = ratioOfOriginalAsset) ?: run {
                if (downloadAsset(it)) getImageDrawableFromAsset(it, forceCropRatio = ratioOfOriginalAsset)
                else null
            }
        }
    }

    override fun getImageRatio(name: String, formatName: Format.Name?): Float? {
        return getAssetFromName(name)?.let { asset ->
            asset.formats?.get(formatName?.formatName)?.size?.let { formatSize ->
                formatSize.width.toFloat() / formatSize.height.toFloat()
            } ?: asset.ratio
        }
    }

    override fun getImageDrawable(
        name: String?,
        hideIfUnknown: Boolean,
        hideIfLoading: Boolean,
        formatName: Format.Name?,
    ): Flow<ImageResult> = flow {
        val imageAsset = getAssetFromName(name)
        if (imageAsset == null) {
            emit(
                UNKNOWN(
                    if (!hideIfUnknown) getPlaceholderDrawable()
                    else null
                )
            )
            return@flow
        }

        val drawable = if (assetsManager.availableAssets().contains(imageAsset)) {
            getImageDrawableFromAsset(
                imageAsset,
                formatName
            )
        } else {
            val loadingDrawable = if (!hideIfLoading) {
                ShimmerDrawable(imageAsset.ratio).apply {
                    val shimmer = Shimmer.ColorHighlightBuilder()
                        .setDuration(900)
                        .setBaseColor(ContextCompat.getColor(context, R.color.undetermined_state))
                        .setHighlightColor(Color.WHITE)
                        .setTilt(0f)
                        .setRepeatDelay(1200)
                        .setDropoff(0.7f)
                        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                        .setAutoStart(true)
                        .build()
                    this.shimmer = shimmer
                }
            } else null
            emit(LOADING(loadingDrawable))

            if (downloadAsset(imageAsset)) {
                getImageDrawableFromAsset(
                    imageAsset,
                    formatName
                )
            } else {
                null
            }
        }

        if (drawable != null) {
            emit(READY(drawable))
        } else {
            val failedDrawable = if (!hideIfLoading) {
                getFailedImage(imageAsset.ratio) ?: run {
                    GradientDrawable().apply {
                        setColor(ContextCompat.getColor(context, R.color.undetermined_state))
                        setSize((imageAsset.ratio * 1000).toInt(), 1000)
                    }
                }
            } else null
            emit(FAILED(failedDrawable))
        }
    }

    private suspend fun getImageDrawableFromAsset(
        imageAsset: Asset,
        formatName: Format.Name? = null,
        forceCropRatio: Float? = null,
    ): Drawable? = withContext(scope.coroutineContext) {
        return@withContext try {
            assetsManager.getAssetInputStream(imageAsset)?.let { inputStream ->
                val extension = imageAsset.name.split(".").last()
                return@withContext if (extension.contains("svg")) {
                    loadSvgFile(inputStream)
                } else if (extension.contains("gif") && buildConfigProvider.sdkInt != Build.VERSION_CODES.Q) {
                    // GIFs will cause native crashes on Android 10 specifically.
                    // Skip loading them as a gif, and just load as a normal image
                    // https://issuetracker.google.com/issues/139371066
                    loadGifFile(inputStream)
                } else {
                    val fullBitmap = BitmapFactory.decodeStream(inputStream)
                    val formattedBitmap = formatName?.let { imageAsset.formats?.get(it.formatName) }?.let {
                        try {
                            fullBitmap.crop(it)
                        } catch (throwable: Throwable) {
                            logger.e(
                                "Couldn't load the cropped image, loading the original now...",
                                throwable = throwable
                            )
                            null
                        }
                    } ?: fullBitmap
                    val resultBitmap = forceCropRatio?.let {
                        ThumbnailUtils.extractThumbnail(
                            formattedBitmap,
                            formattedBitmap.width,
                            (formattedBitmap.width / forceCropRatio).toInt(),
                        )
                    } ?: formattedBitmap

                    try {
                        inputStream.close()
                    } catch (e: Error) {
                        App.log.e("Couldn't close input stream for image asset: ${imageAsset.name}", "ImageService", e)
                    }
                    BitmapDrawable(resources, resultBitmap)
                }
            }
        } catch (error: Throwable) {
            if (error !is FileNotFoundException) {
                logger.e(
                    message = "Image ${imageAsset.name} couldn't be loaded",
                    throwable = error
                )
            }
            null
        }
    }

    override fun isImageKnown(name: String): Boolean {
        val localizedName = localizationService.getString(name)
        return assetsManager.assetsFromConfig().find { it.name == localizedName } != null
    }

    override fun isImageAvailable(name: String): Boolean {
        val localizedName = localizationService.getString(name)
        return runBlocking {
            withContext(scope.coroutineContext) {
                assetsManager.availableAssets().find { it.name == localizedName } != null
            }
        }
    }

    private suspend fun downloadAsset(assetImage: Asset): Boolean {
        return try {
            assetsManager.downloadAsset(assetImage)
            true
        } catch (throwable: Throwable) {
            logger.e("Couldn't download image ${assetImage.name}", throwable = throwable)
            false
        }
    }

    private fun loadSvgFile(assetInputStream: InputStream): Drawable? {
        return try {
            loadInputStream(assetInputStream).drawable.toBitmapDrawable()
        } catch (error: Throwable) {
            logger.e(message = "Parsing of SVG file failed", throwable = error)
            null
        }
    }

    private fun loadGifFile(assetInputStream: InputStream): AnimatedImageDrawable {
        val source = ImageDecoder.createSource(ByteBuffer.wrap(assetInputStream.readBytes()))
        val drawable = ImageDecoder.decodeDrawable(source)
        return drawable as AnimatedImageDrawable
    }

    /** Used to convert a PictureDrawable to a BitmapDrawable. BitmapDrawable is better for setting tint or [ColorFilter].**/
    private fun PictureDrawable.toBitmapDrawable(): BitmapDrawable {
        val bitmap = Bitmap.createBitmap(
            this.intrinsicWidth,
            this.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawPicture(this.picture)
        return BitmapDrawable(resources, bitmap)
    }

}

public sealed class ImageResult(public val drawable: Drawable?) {
    public class READY(drawable: Drawable) : ImageResult(drawable)
    public class FAILED(drawable: Drawable?) : ImageResult(drawable)
    public class UNKNOWN(drawable: Drawable?) : ImageResult(drawable)
    public class LOADING(drawable: Drawable?) : ImageResult(drawable)
}
