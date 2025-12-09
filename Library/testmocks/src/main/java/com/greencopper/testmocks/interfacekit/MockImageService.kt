package com.greencopper.testmocks.interfacekit

import android.graphics.drawable.ShapeDrawable
import com.greencopper.core.asset.recipe.Asset
import com.greencopper.interfacekit.imageservice.ImageResult
import com.greencopper.interfacekit.imageservice.ImageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockImageService : ImageService {
    public var resultReturned: ImageResult? = null
    private val defaultDrawable by lazy {
        ShapeDrawable().apply {
            intrinsicHeight = 50
            intrinsicWidth = 50
        }
    }

    public var getImageDrawable_name: String? = null
    override fun getImageDrawable(
        name: String?,
        hideIfUnknown: Boolean,
        hideIfLoading: Boolean,
        formatName: Asset.Format.Name?,
    ): Flow<ImageResult> {
        getImageDrawable_name = name
        return flowOf(
            resultReturned ?: ImageResult.UNKNOWN(
                defaultDrawable
            )
        )
    }

    public var getImageRatio_name: String? = null
    public var getImageRatio_formatName: Asset.Format.Name? = null
    public var getImageRatio_result: Float? = null
    override fun getImageRatio(name: String, formatName: Asset.Format.Name?): Float? {
        getImageRatio_name = name
        getImageRatio_formatName = formatName
        return getImageRatio_result
    }

    public var isImageKnown_name: String? = null
    public var isImageKnown_result: Boolean = false
    override fun isImageKnown(name: String): Boolean {
        isImageKnown_name = name
        return isImageKnown_result
    }

    public var isImageAvailable_name: String? = null
    public var isImageAvailable_result: Boolean = false
    override fun isImageAvailable(name: String): Boolean {
        isImageAvailable_name = name
        return isImageAvailable_result
    }
}
