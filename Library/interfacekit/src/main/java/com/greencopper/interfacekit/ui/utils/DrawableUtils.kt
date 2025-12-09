package com.greencopper.interfacekit.ui.utils

import android.content.Context
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RectShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toBitmap

public fun Drawable.scale(context: Context, maxDimension: Int): Drawable {
    val bitmap = if (intrinsicHeight > intrinsicWidth) {
        toBitmap((maxDimension * (intrinsicWidth.toFloat() / intrinsicHeight.toFloat())).toInt(), maxDimension)
    } else {
        toBitmap(maxDimension, (maxDimension * (intrinsicHeight.toFloat() / intrinsicWidth.toFloat())).toInt())
    }

    return BitmapDrawable(context.resources, bitmap)
}

@Composable
public fun createRect(width: Dp, height: Dp, color: Color): Drawable = with(LocalDensity.current) {
    val shape = ShapeDrawable(RectShape())
    shape.intrinsicHeight = height.roundToPx()
    shape.intrinsicWidth = width.roundToPx()
    shape.setTint(color.toArgb())
    return shape
}
