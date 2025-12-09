package com.greencopper.maps.geomap.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.ui.spToPx
import com.greencopper.kiba_maps.R
import com.greencopper.maps.geomap.ImageProperties
import com.greencopper.maps.geomap.data.MapData

internal fun createPinImage(
    context: Context,
    glyph: Drawable,
    props: ImageProperties,
): Bitmap {
    val background = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null)!!

    val multiplier = if (props.selected) 2 else 1
    val outlineSize = mapPinSize * multiplier
    val backgroundSize = outlineSize - 16
    val backgroundOffset = 8.0f
    val glyphSize = glyphSize * multiplier
    val glyphOffset = (outlineSize / 2) - (glyphSize / 2)

    val outlineBitmap = background.toBitmap(outlineSize, outlineSize)
    val outlinePaint = Paint().apply { colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }
    val backgroundBitmap = background.toBitmap(backgroundSize, backgroundSize)
    val backgroundPaint =
        Paint().apply { colorFilter = PorterDuffColorFilter(props.backgroundColor, PorterDuff.Mode.SRC_ATOP) }
    val glyphBitmap = glyph.toBitmap(glyphSize, glyphSize)
    val glyphPaint =
        Paint().apply { colorFilter = PorterDuffColorFilter(props.glyphColor, PorterDuff.Mode.SRC_ATOP) }

    val combinedBitmap =
        Bitmap.createBitmap(outlineBitmap.width, outlineBitmap.height, outlineBitmap.config ?: Bitmap.Config.ARGB_8888)
    val canvas = Canvas(combinedBitmap)
    canvas.drawBitmap(outlineBitmap, 0f, 0f, outlinePaint)
    canvas.drawBitmap(backgroundBitmap, backgroundOffset, backgroundOffset, backgroundPaint)
    canvas.drawBitmap(glyphBitmap, glyphOffset.toFloat(), glyphOffset.toFloat(), glyphPaint)

    return combinedBitmap
}

internal fun createDotImage(
    context: Context,
    props: ImageProperties,
): Bitmap {
    val dot = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null)!!
    val backgroundSize = 12.dpToPx()
    val dotSize = backgroundSize - 8
    val dotOffset = 4.0f

    val backgroundBitmap = dot.toBitmap(backgroundSize, backgroundSize)
    val backgroundPaint =
        Paint().apply { colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }
    val dotBitmap = dot.toBitmap(dotSize, dotSize)
    val glyphPaint =
        Paint().apply { colorFilter = PorterDuffColorFilter(props.backgroundColor, PorterDuff.Mode.SRC_ATOP) }

    val combinedBitmap =
        Bitmap.createBitmap(backgroundBitmap.width, backgroundBitmap.height, backgroundBitmap.config ?: Bitmap.Config.ARGB_8888)
    val canvas = Canvas(combinedBitmap)
    canvas.drawBitmap(backgroundBitmap, 0f, 0f, backgroundPaint)
    canvas.drawBitmap(dotBitmap, dotOffset, dotOffset, glyphPaint)

    return combinedBitmap
}

internal fun createLabelItem(feature: MapData.Feature, label: String, isSelected: Boolean): MapItem.MapLabelItem {
    val textPaint = TextPaint()
    textPaint.isAntiAlias = true
    textPaint.textSize = 16.spToPx()
    textPaint.typeface = typeface
    textPaint.color = Color.BLACK

    val strokePaint = Paint()
    strokePaint.isAntiAlias = true
    strokePaint.textSize = 16.spToPx()
    strokePaint.typeface = typeface
    strokePaint.style = Paint.Style.STROKE
    strokePaint.strokeWidth = 8f
    strokePaint.color = Color.WHITE

    val baseline = -strokePaint.ascent() // ascent() is negative
    val width = (strokePaint.measureText(label) + strokePaint.strokeWidth + 0.5f).toInt()
    val height = (baseline + strokePaint.descent() + strokePaint.strokeWidth + 0.5f).toInt()

    // Some padding is added to the top so the label doesn't overlap with the corresponding map pin
    // but we can still anchor it to the correct position
    val multiplier = if (isSelected) 2 else 1
    val offset = (mapPinSize * multiplier) / 2

    val bitmap = Bitmap.createBitmap(width, height + offset, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawText(label, 0f, baseline + offset, strokePaint)
    canvas.drawText(label, 0f, baseline + offset, textPaint)

    return MapItem.MapLabelItem(feature, bitmap, offset)
}

internal const val MIN_LAT = -90.0
internal const val MAX_LAT = 90.0
internal const val MIN_LON = -180.0
internal const val MAX_LON = 180.0

internal fun getDefaultOverlayBounds() = LatLngBounds.Builder()
    .include(LatLng(MIN_LAT, MIN_LON))
    .include(LatLng(MAX_LAT, MAX_LON))
    .include(LatLng(0.0, 0.0)).build()

private val typeface = Typeface.create(Typeface.DEFAULT, 600, false)

private val mapPinSize: Int by lazy { 46.dpToPx() }
private val glyphSize: Int by lazy { 20.dpToPx() }
