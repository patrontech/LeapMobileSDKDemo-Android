/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.greencopper.interfacekit.ui.utils.shimmerdrawable

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.*
import com.greencopper.interfacekit.R
import kotlin.math.*

/**
 * A Shimmer is an object detailing all of the configuration options available for [ ]
 */
internal class Shimmer internal constructor() {
    /**
     * The shape of the shimmer's highlight. By default LINEAR is used.
     */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(Shape.LINEAR, Shape.RADIAL)
    annotation class Shape {
        companion object {
            /**
             * Linear gives a ray reflection effect.
             */
            const val LINEAR = 0

            /**
             * Radial gives a spotlight effect.
             */
            const val RADIAL = 1
        }
    }

    /**
     * Direction of the shimmer's sweep.
     */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(Direction.LEFT_TO_RIGHT, Direction.TOP_TO_BOTTOM, Direction.RIGHT_TO_LEFT, Direction.BOTTOM_TO_TOP)
    annotation class Direction {
        companion object {
            const val LEFT_TO_RIGHT = 0
            const val TOP_TO_BOTTOM = 1
            const val RIGHT_TO_LEFT = 2
            const val BOTTOM_TO_TOP = 3
        }
    }

    val positions = FloatArray(COMPONENT_COUNT)
    val colors = IntArray(COMPONENT_COUNT)
    val bounds = RectF()

    @Direction
    var direction = Direction.LEFT_TO_RIGHT

    @ColorInt
    var highlightColor = Color.WHITE

    @ColorInt
    var baseColor = 0x4cffffff

    @Shape
    var shape = Shape.LINEAR
    var fixedWidth = 0
    var fixedHeight = 0
    var widthRatio = 1f
    var heightRatio = 1f
    var intensity = 0f
    var dropoff = 0.5f
    var tilt = 20f
    var clipToChildren = true
    var autoStart = true
    var alphaShimmer = true
    var repeatCount = ValueAnimator.INFINITE
    var repeatMode = ValueAnimator.RESTART
    var animationDuration = 1000L
    var repeatDelay: Long = 0
    var startDelay: Long = 0
    fun width(width: Int): Int {
        return if (fixedWidth > 0) fixedWidth else Math.round(widthRatio * width)
    }

    fun height(height: Int): Int {
        return if (fixedHeight > 0) fixedHeight else Math.round(heightRatio * height)
    }

    fun updateColors() {
        when (shape) {
            Shape.LINEAR -> {
                colors[0] = baseColor
                colors[1] = highlightColor
                colors[2] = highlightColor
                colors[3] = baseColor
            }

            Shape.RADIAL -> {
                colors[0] = highlightColor
                colors[1] = highlightColor
                colors[2] = baseColor
                colors[3] = baseColor
            }
        }
    }

    fun updatePositions() {
        when (shape) {
            Shape.LINEAR -> {
                positions[0] = max((1f - intensity - dropoff) / 2f, 0f)
                positions[1] = max((1f - intensity - 0.001f) / 2f, 0f)
                positions[2] = min((1f + intensity + 0.001f) / 2f, 1f)
                positions[3] = min((1f + intensity + dropoff) / 2f, 1f)
            }

            Shape.RADIAL -> {
                positions[0] = 0f
                positions[1] = min(intensity, 1f)
                positions[2] = min(intensity + dropoff, 1f)
                positions[3] = 1f
            }
        }
    }

    fun updateBounds(viewWidth: Int, viewHeight: Int) {
        val magnitude = max(viewWidth, viewHeight)
        val rad = Math.PI / 2f - Math.toRadians((tilt % 90f).toDouble())
        val hyp = magnitude / sin(rad)
        val padding = 3 * ((hyp - magnitude).toFloat() / 2f).roundToInt()
        bounds[-padding.toFloat(), -padding.toFloat(), (width(viewWidth) + padding).toFloat()] =
            (height(viewHeight) + padding).toFloat()
    }

    abstract class Builder<T : Builder<T>?> {
        val mShimmer = Shimmer()

        protected abstract fun getThis(): T

        /**
         * Applies all specified options from the [AttributeSet].
         */
        fun consumeAttributes(context: Context, attrs: AttributeSet?): T {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerFrameLayout, 0, 0)
            return consumeAttributes(a)
        }

        open fun consumeAttributes(attributes: TypedArray): T {
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_clip_to_children)) {
                setClipToChildren(
                    attributes.getBoolean(
                        R.styleable.ShimmerFrameLayout_shimmer_clip_to_children, mShimmer.clipToChildren
                    )
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_auto_start)) {
                setAutoStart(
                    attributes.getBoolean(R.styleable.ShimmerFrameLayout_shimmer_auto_start, mShimmer.autoStart)
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_base_alpha)) {
                setBaseAlpha(attributes.getFloat(R.styleable.ShimmerFrameLayout_shimmer_base_alpha, 0.3f))
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_highlight_alpha)) {
                setHighlightAlpha(attributes.getFloat(R.styleable.ShimmerFrameLayout_shimmer_highlight_alpha, 1f))
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_duration)) {
                setDuration(
                    attributes.getInt(
                        R.styleable.ShimmerFrameLayout_shimmer_duration, mShimmer.animationDuration.toInt()
                    ).toLong()
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_repeat_count)) {
                setRepeatCount(
                    attributes.getInt(R.styleable.ShimmerFrameLayout_shimmer_repeat_count, mShimmer.repeatCount)
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_repeat_delay)) {
                setRepeatDelay(
                    attributes.getInt(
                        R.styleable.ShimmerFrameLayout_shimmer_repeat_delay, mShimmer.repeatDelay.toInt()
                    ).toLong()
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_repeat_mode)) {
                setRepeatMode(
                    attributes.getInt(R.styleable.ShimmerFrameLayout_shimmer_repeat_mode, mShimmer.repeatMode)
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_start_delay)) {
                setStartDelay(
                    attributes.getInt(
                        R.styleable.ShimmerFrameLayout_shimmer_start_delay, mShimmer.startDelay.toInt()
                    ).toLong()
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_direction)) {
                when (attributes.getInt(R.styleable.ShimmerFrameLayout_shimmer_direction, mShimmer.direction)) {
                    Direction.LEFT_TO_RIGHT -> setDirection(Direction.LEFT_TO_RIGHT)
                    Direction.TOP_TO_BOTTOM -> setDirection(Direction.TOP_TO_BOTTOM)
                    Direction.RIGHT_TO_LEFT -> setDirection(Direction.RIGHT_TO_LEFT)
                    Direction.BOTTOM_TO_TOP -> setDirection(Direction.BOTTOM_TO_TOP)
                    else -> setDirection(Direction.LEFT_TO_RIGHT)
                }
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_shape)) {
                when (attributes.getInt(R.styleable.ShimmerFrameLayout_shimmer_shape, mShimmer.shape)) {
                    Shape.LINEAR -> setShape(Shape.LINEAR)
                    Shape.RADIAL -> setShape(Shape.RADIAL)
                    else -> setShape(Shape.LINEAR)
                }
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_dropoff)) {
                setDropoff(attributes.getFloat(R.styleable.ShimmerFrameLayout_shimmer_dropoff, mShimmer.dropoff))
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_fixed_width)) {
                setFixedWidth(
                    attributes.getDimensionPixelSize(
                        R.styleable.ShimmerFrameLayout_shimmer_fixed_width, mShimmer.fixedWidth
                    )
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_fixed_height)) {
                setFixedHeight(
                    attributes.getDimensionPixelSize(
                        R.styleable.ShimmerFrameLayout_shimmer_fixed_height, mShimmer.fixedHeight
                    )
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_intensity)) {
                setIntensity(
                    attributes.getFloat(R.styleable.ShimmerFrameLayout_shimmer_intensity, mShimmer.intensity)
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_width_ratio)) {
                setWidthRatio(
                    attributes.getFloat(R.styleable.ShimmerFrameLayout_shimmer_width_ratio, mShimmer.widthRatio)
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_height_ratio)) {
                setHeightRatio(
                    attributes.getFloat(R.styleable.ShimmerFrameLayout_shimmer_height_ratio, mShimmer.heightRatio)
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_tilt)) {
                setTilt(attributes.getFloat(R.styleable.ShimmerFrameLayout_shimmer_tilt, mShimmer.tilt))
            }
            return getThis()
        }

        /**
         * Copies the configuration of an already built Shimmer to this builder
         */
        fun copyFrom(other: Shimmer): T {
            setDirection(other.direction)
            setShape(other.shape)
            setFixedWidth(other.fixedWidth)
            setFixedHeight(other.fixedHeight)
            setWidthRatio(other.widthRatio)
            setHeightRatio(other.heightRatio)
            setIntensity(other.intensity)
            setDropoff(other.dropoff)
            setTilt(other.tilt)
            setClipToChildren(other.clipToChildren)
            setAutoStart(other.autoStart)
            setRepeatCount(other.repeatCount)
            setRepeatMode(other.repeatMode)
            setRepeatDelay(other.repeatDelay)
            setStartDelay(other.startDelay)
            setDuration(other.animationDuration)
            mShimmer.baseColor = other.baseColor
            mShimmer.highlightColor = other.highlightColor
            return getThis()
        }

        /**
         * Sets the direction of the shimmer's sweep. See [Direction].
         */
        fun setDirection(@Direction direction: Int): T {
            mShimmer.direction = direction
            return getThis()
        }

        /**
         * Sets the shape of the shimmer. See [Shape].
         */
        fun setShape(@Shape shape: Int): T {
            mShimmer.shape = shape
            return getThis()
        }

        /**
         * Sets the fixed width of the shimmer, in pixels.
         */
        fun setFixedWidth(@Px fixedWidth: Int): T {
            require(fixedWidth >= 0) { "Given invalid width: $fixedWidth" }
            mShimmer.fixedWidth = fixedWidth
            return getThis()
        }

        /**
         * Sets the fixed height of the shimmer, in pixels.
         */
        fun setFixedHeight(@Px fixedHeight: Int): T {
            require(fixedHeight >= 0) { "Given invalid height: $fixedHeight" }
            mShimmer.fixedHeight = fixedHeight
            return getThis()
        }

        /**
         * Sets the width ratio of the shimmer, multiplied against the total width of the layout.
         */
        fun setWidthRatio(widthRatio: Float): T {
            require(widthRatio >= 0f) { "Given invalid width ratio: $widthRatio" }
            mShimmer.widthRatio = widthRatio
            return getThis()
        }

        /**
         * Sets the height ratio of the shimmer, multiplied against the total height of the layout.
         */
        fun setHeightRatio(heightRatio: Float): T {
            require(heightRatio >= 0f) { "Given invalid height ratio: $heightRatio" }
            mShimmer.heightRatio = heightRatio
            return getThis()
        }

        /**
         * Sets the intensity of the shimmer. A larger value causes the shimmer to be larger.
         */
        fun setIntensity(intensity: Float): T {
            require(intensity >= 0f) { "Given invalid intensity value: $intensity" }
            mShimmer.intensity = intensity
            return getThis()
        }

        /**
         * Sets how quickly the shimmer's gradient drops-off. A larger value causes a sharper drop-off.
         */
        fun setDropoff(dropoff: Float): T {
            require(dropoff >= 0f) { "Given invalid dropoff value: $dropoff" }
            mShimmer.dropoff = dropoff
            return getThis()
        }

        /**
         * Sets the tilt angle of the shimmer in degrees.
         */
        fun setTilt(tilt: Float): T {
            mShimmer.tilt = tilt
            return getThis()
        }

        /**
         * Sets the base alpha, which is the alpha of the underlying children, amount in the range [0,
         * 1].
         */
        fun setBaseAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
            val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
            mShimmer.baseColor = intAlpha shl 24 or (mShimmer.baseColor and 0x00FFFFFF)
            return getThis()
        }

        /**
         * Sets the shimmer alpha amount in the range [0, 1].
         */
        fun setHighlightAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
            val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
            mShimmer.highlightColor = intAlpha shl 24 or (mShimmer.highlightColor and 0x00FFFFFF)
            return getThis()
        }

        /**
         * Sets whether the shimmer will clip to the childrens' contents, or if it will opaquely draw on
         * top of the children.
         */
        fun setClipToChildren(status: Boolean): T {
            mShimmer.clipToChildren = status
            return getThis()
        }

        /**
         * Sets whether the shimmering animation will start automatically.
         */
        fun setAutoStart(status: Boolean): T {
            mShimmer.autoStart = status
            return getThis()
        }

        /**
         * Sets how often the shimmering animation will repeat. See [ ][android.animation.ValueAnimator.setRepeatCount].
         */
        fun setRepeatCount(repeatCount: Int): T {
            mShimmer.repeatCount = repeatCount
            return getThis()
        }

        /**
         * Sets how the shimmering animation will repeat. See [ ][android.animation.ValueAnimator.setRepeatMode].
         */
        fun setRepeatMode(mode: Int): T {
            mShimmer.repeatMode = mode
            return getThis()
        }

        /**
         * Sets how long to wait in between repeats of the shimmering animation.
         */
        fun setRepeatDelay(millis: Long): T {
            require(millis >= 0) { "Given a negative repeat delay: $millis" }
            mShimmer.repeatDelay = millis
            return getThis()
        }

        /**
         * Sets how long to wait for starting the shimmering animation.
         */
        fun setStartDelay(millis: Long): T {
            require(millis >= 0) { "Given a negative start delay: $millis" }
            mShimmer.startDelay = millis
            return getThis()
        }

        /**
         * Sets how long the shimmering animation takes to do one full sweep.
         */
        fun setDuration(millis: Long): T {
            require(millis >= 0) { "Given a negative duration: $millis" }
            mShimmer.animationDuration = millis
            return getThis()
        }

        fun build(): Shimmer {
            mShimmer.updateColors()
            mShimmer.updatePositions()
            return mShimmer
        }

        companion object {
            private fun clamp(min: Float, max: Float, value: Float): Float {
                return min(max, max(min, value))
            }
        }
    }

    class AlphaHighlightBuilder : Builder<AlphaHighlightBuilder>() {
        init {
            mShimmer.alphaShimmer = true
        }

        override fun getThis(): AlphaHighlightBuilder = this
    }

    class ColorHighlightBuilder : Builder<ColorHighlightBuilder>() {
        init {
            mShimmer.alphaShimmer = false
        }

        override fun getThis(): ColorHighlightBuilder = this

        /**
         * Sets the highlight color for the shimmer.
         */
        fun setHighlightColor(@ColorInt color: Int): ColorHighlightBuilder {
            mShimmer.highlightColor = color
            return this
        }

        /**
         * Sets the base color for the shimmer.
         */
        fun setBaseColor(@ColorInt color: Int): ColorHighlightBuilder {
            mShimmer.baseColor = color
            return this
        }

        override fun consumeAttributes(attributes: TypedArray): ColorHighlightBuilder {
            super.consumeAttributes(attributes)
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_base_color)) {
                setBaseColor(
                    attributes.getColor(R.styleable.ShimmerFrameLayout_shimmer_base_color, mShimmer.baseColor)
                )
            }
            if (attributes.hasValue(R.styleable.ShimmerFrameLayout_shimmer_highlight_color)) {
                setHighlightColor(
                    attributes.getColor(
                        R.styleable.ShimmerFrameLayout_shimmer_highlight_color, mShimmer.highlightColor
                    )
                )
            }
            return this
        }

    }

    companion object {
        private const val COMPONENT_COUNT = 4
    }
}
