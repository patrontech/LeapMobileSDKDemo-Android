package com.greencopper.interfacekit.imageservice

import android.graphics.Bitmap
import android.graphics.Matrix
import com.greencopper.core.asset.recipe.Asset

internal fun Bitmap.crop(
    format: Asset.Format,
): Bitmap = Bitmap.createBitmap(
    this,
    format.origin.x,
    format.origin.y,
    format.size.width,
    format.size.height
)

public fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
