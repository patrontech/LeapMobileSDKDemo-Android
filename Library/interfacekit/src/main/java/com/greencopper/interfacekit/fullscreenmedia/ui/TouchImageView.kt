package com.greencopper.interfacekit.fullscreenmedia.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.*
import androidx.appcompat.widget.AppCompatImageView

internal class TouchImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle), View.OnTouchListener,
    GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private var scaleDetector: ScaleGestureDetector
    private var gestureDetector: GestureDetector
    private var localMatrix: Matrix
    private var matrixValues: FloatArray
    private var mode = ImageState.NONE

    // Scales
    private var saveScale = 1f
    private val minScale = 1f
    private val maxScale = 4f

    // view dimensions
    private var origWidth = 0f
    private var origHeight = 0f
    private var viewWidth = 0f
    private var viewHeight = 0f
    private var last = PointF()
    private var start = PointF()

    init {
        super.setClickable(true)
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        localMatrix = Matrix()
        matrixValues = FloatArray(9)
        imageMatrix = localMatrix
        scaleType = ScaleType.MATRIX
        gestureDetector = GestureDetector(context, this)
        setOnTouchListener(this)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ImageState.ZOOM
            movedDuringDoubleTap = true
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Setup new scaleFactor based on gesture captured by detector
            scaleTo(detector.scaleFactor, detector.focusX, detector.focusY, true)
            return true
        }
    }

    private fun scaleTo(scale: Float, focusX: Float, focusY: Float, isFactor: Boolean) {
        var scaleToApply = if (isFactor) {
            scale
        } else {
            scale / saveScale
        }

        val prevScale = saveScale

        saveScale *= scaleToApply

        if (saveScale > maxScale) {
            saveScale = maxScale
            scaleToApply = maxScale / prevScale
        } else if (saveScale < minScale) {
            saveScale = minScale
            scaleToApply = minScale / prevScale
        }

        // Apply new scale on the matrix of the image
        if (origWidth * saveScale <= viewWidth
            || origHeight * saveScale <= viewHeight
        ) {
            localMatrix.postScale(
                scaleToApply, scaleToApply, viewWidth / 2f,
                viewHeight / 2f
            )
        } else {
            localMatrix.postScale(
                scaleToApply, scaleToApply,
                focusX, focusY
            )
        }
        fixTranslation()
    }

    private fun fitToScreen() {
        saveScale = 1f
        val scale: Float
        drawable?.let {
            if (it.intrinsicHeight == 0 || it.intrinsicWidth == 0) return

            val imageWidth = it.intrinsicWidth.toFloat()
            val imageHeight = it.intrinsicHeight.toFloat()
            val scaleX = viewWidth / imageWidth
            val scaleY = viewHeight / imageHeight
            scale = scaleX.coerceAtMost(scaleY)
            localMatrix.setScale(scale, scale)

            // Center the image
            val redundantYSpace = (viewHeight - scale * imageHeight)
            val redundantXSpace = (viewWidth - scale * imageWidth)
            origWidth = viewWidth - redundantXSpace
            origHeight = viewHeight - redundantYSpace
            localMatrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2)
            imageMatrix = localMatrix
        }
    }

    private fun fixTranslation() {
        localMatrix.getValues(matrixValues) // put matrix values into a float array so we can analyze
        val transX = matrixValues[Matrix.MTRANS_X] // get the most recent translation in x direction
        val transY = matrixValues[Matrix.MTRANS_Y] // get the most recent translation in y direction
        val fixTransX = getFixTranslation(transX, viewWidth, origWidth * saveScale)
        val fixTransY = getFixTranslation(transY, viewHeight, origHeight * saveScale)
        if (fixTransX != 0f || fixTransY != 0f) {
            localMatrix.postTranslate(fixTransX, fixTransY)
        }
    }

    private fun getFixTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) { // case: NOT ZOOMED
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else { // CASE: ZOOMED
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }
        if (trans < minTrans) { // negative x or y translation (down or to the right)
            return -trans + minTrans
        }
        if (trans > maxTrans) { // positive x or y translation (up or to the left)
            return -trans + maxTrans
        }
        return 0F
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            0F
        } else delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        viewHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        if (saveScale == 1f) {
            // Fit to screen.
            fitToScreen()
        }
    }

    /*
        Ontouch
     */
    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        val currentPoint = PointF(event.x, event.y)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                last.set(currentPoint)
                start.set(last)
                mode = ImageState.DRAG
            }
            MotionEvent.ACTION_MOVE -> if (mode == ImageState.DRAG) {
                val dx = currentPoint.x - last.x
                val dy = currentPoint.y - last.y
                val fixTransX = getFixDragTrans(dx, viewWidth, origWidth * saveScale)
                val fixTransY = getFixDragTrans(dy, viewHeight, origHeight * saveScale)
                localMatrix.postTranslate(fixTransX, fixTransY)
                fixTranslation()
                last[currentPoint.x] = currentPoint.y
            }

            MotionEvent.ACTION_POINTER_UP -> mode = ImageState.NONE
            MotionEvent.ACTION_UP -> {
                mode = ImageState.NONE
                movedDuringDoubleTap = false
            }
        }
        imageMatrix = localMatrix
        return false
    }

    /*
        GestureListener
     */
    override fun onDown(motionEvent: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(motionEvent: MotionEvent) {}
    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(p0: MotionEvent?, motionEvent: MotionEvent, v: Float, v1: Float): Boolean {
        return false
    }

    override fun onLongPress(motionEvent: MotionEvent) {}
    override fun onFling(p0: MotionEvent?, motionEvent: MotionEvent, v: Float, v1: Float): Boolean {
        return false
    }

    /*
        onDoubleTap
     */
    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTap(motionEvent: MotionEvent): Boolean {
        return false
    }

    private var movedDuringDoubleTap = false

    override fun onDoubleTapEvent(motionEvent: MotionEvent): Boolean {

        if (motionEvent.actionMasked == MotionEvent.ACTION_UP) {
            if (movedDuringDoubleTap.not()) {
                val midMaxScale = maxScale / 2

                when (saveScale) {
                    maxScale -> fitToScreen()
                    in (midMaxScale.rangeTo(maxScale)) -> scaleTo(
                        maxScale,
                        motionEvent.x,
                        motionEvent.y,
                        false
                    )

                    in (minScale..midMaxScale) -> scaleTo(
                        midMaxScale,
                        motionEvent.x,
                        motionEvent.y,
                        false
                    )

                    else -> fitToScreen()
                }
            }
        }

        return true
    }

    enum class ImageState {
        NONE,
        DRAG,
        ZOOM
    }
}
