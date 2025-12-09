package com.greencopper.interfacekit.imageservice

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.greencopper.core.asset.recipe.Asset
import com.greencopper.interfacekit.ui.utils.createRect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.random.Random
import androidx.compose.ui.graphics.Color.Companion as ColorCompose

//TODO Improve rendering by taking into account more than just the imageName, emulate placeholder and failed image, etc...
public fun randomize(service: ImageService) : ImageService = object : ImageService {
    override fun isImageKnown(name: String): Boolean = service.isImageKnown(name)
    override fun isImageAvailable(name: String): Boolean = service.isImageAvailable(name)
    override fun getImageRatio(name: String, formatName: Asset.Format.Name?): Float? = service.getImageRatio(name, formatName)

    override fun getImageDrawable(
        name: String?,
        hideIfUnknown: Boolean,
        hideIfLoading: Boolean,
        formatName: Asset.Format.Name?
    ): Flow<ImageResult> = when (Random.nextInt(0, 4)) {
        0 -> flowOf(ImageResult.UNKNOWN(null))
        1 -> flowOf(ImageResult.FAILED(null))
        2 -> flowOf(ImageResult.LOADING(null))
        else -> service.getImageDrawable(name, hideIfUnknown, hideIfLoading, formatName)
    }
}

@Composable
public fun mockComposeImageService(
    map: Map<String, Drawable> = mapOf(),
    failIfMissing: Boolean = false,
): ImageService {

    val defaultRect = createRect(width = 40.dp, height = 40.dp, color = ColorCompose.Red)
    fun getDrawable(key: String): Drawable = map[key] ?: if (failIfMissing) {
        error("Image `$key` not mocked")
    } else {
        defaultRect
    }

    return object : ImageService {
        override fun getImageDrawable(
            name: String?,
            hideIfUnknown: Boolean,
            hideIfLoading: Boolean,
            formatName: Asset.Format.Name?
        ): Flow<ImageResult> = flowOf(ImageResult.READY(getDrawable(key = name ?: "error")))

        override fun isImageKnown(name: String): Boolean = true

        override fun isImageAvailable(name: String): Boolean = true

        override fun getImageRatio(name: String, formatName: Asset.Format.Name?): Float? = 1f
    }
}
