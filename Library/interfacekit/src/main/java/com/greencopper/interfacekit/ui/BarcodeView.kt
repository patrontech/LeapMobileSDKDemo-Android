package com.greencopper.interfacekit.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.greencopper.interfacekit.R
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.d
import kotlinx.parcelize.Parcelize
import kotlin.math.min

public class BarcodeView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attributeSet, defStyleAttr) {

    // To enable instanceState save and restore
    override fun isSaveEnabled(): Boolean = true

    private var value: String = ""
        set(value) {
            if (value != field) {
                field = value
                requestLayout()
                postInvalidate()
            }
        }

    private var barcodeFormat: BarcodeFormat = BarcodeFormat.QR_CODE
        set(value) {
            if (value != field) {
                field = value
                requestLayout()
                postInvalidate()
            }
        }

    public fun setBarcodeValue(value: String) {
        this.value = value
    }

    public fun setBarcodeType(barcodeFormat: BarcodeFormat) {
        this.barcodeFormat = barcodeFormat
    }

    private fun bitmapWidth(): Int {
        if (value.isBlank()) {
            return 0
        }
        return when (barcodeFormat) {
            BarcodeFormat.AZTEC,
            BarcodeFormat.QR_CODE,
            BarcodeFormat.CODABAR,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.UPC_EAN_EXTENSION,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.EAN_8,
            BarcodeFormat.EAN_13,
            BarcodeFormat.ITF,
            BarcodeFormat.MAXICODE,
            BarcodeFormat.PDF_417,
            BarcodeFormat.RSS_14,
            BarcodeFormat.RSS_EXPANDED -> 500
            else -> -1
        }
    }

    private fun bitmapHeight(): Int {
        if (value.isBlank()) {
            return 0
        }
        return when (barcodeFormat) {
            BarcodeFormat.AZTEC,
            BarcodeFormat.QR_CODE,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.MAXICODE,
            BarcodeFormat.RSS_EXPANDED -> 500
            BarcodeFormat.CODABAR,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.UPC_EAN_EXTENSION,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.EAN_8,
            BarcodeFormat.EAN_13,
            BarcodeFormat.ITF,
            BarcodeFormat.PDF_417,
            BarcodeFormat.RSS_14 -> 200
            else -> -1
        }
    }

    init {
        computeAttributesFromXml(attributeSet)
    }

    private fun computeAttributesFromXml(attributeSet: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.BarcodeView,
            0,
            0
        )
        try {
            value = typedArray.getString(R.styleable.BarcodeView_value_barcode) ?: ""
            val barcodeFormatIntValue = typedArray.getInt(R.styleable.BarcodeView_barcodeType, -1)
            if (barcodeFormatIntValue > 0) {
                try {
                    barcodeFormat = barcodeFormatFrom(barcodeFormatIntValue)
                } catch (e: IndexOutOfBoundsException) {
                    App.log.d("Could not compute Barcode type from XML", throwable = e)
                }
            }
        } finally {
            typedArray.recycle()
        }
    }

    @Throws(IndexOutOfBoundsException::class)
    private fun barcodeFormatFrom(ordinal: Int): BarcodeFormat = BarcodeFormat.entries[ordinal]

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(
            super.onSaveInstanceState(),
            value,
            barcodeFormat,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? SavedState)?.let {
            value = it.barcodeValue
            barcodeFormat = it.barcodeFormat
            super.onRestoreInstanceState(it.viewState)
        } ?: super.onRestoreInstanceState(state)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        displayBarcode()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun displayBarcode() {
        computeBarcode()?.let {
            setImageBitmap(it)
        }
    }

    private fun computeBarcode(): Bitmap? {
        val bitmap = createBarcodeBitmap()
        if (bitmap != null && bitmap.height != 0 && bitmap.width != 0) {
            encodeValue()?.let { encodedValue ->
                writeBarcode(bitmap, encodedValue)
            }
        }
        return bitmap
    }

    private fun createBarcodeBitmap(): Bitmap? {
        return try {
            Bitmap.createBitmap(bitmapWidth(), bitmapHeight(), Bitmap.Config.ARGB_8888)
        } catch (e: IllegalArgumentException) {
            App.log.d("Could not create Barcode bitmap", throwable = e)
            null
        }
    }

    private fun encodeValue(): BitMatrix? {
        return try {
            val codeWriter = MultiFormatWriter()
            val hints = mapOf(EncodeHintType.MARGIN to 0)
            codeWriter.encode(value, barcodeFormat, bitmapWidth(), bitmapHeight(), hints)
        } catch (e: IllegalArgumentException) {
            App.log.d("Could not write Barcode bitmap", throwable = e)
            null
        } catch (e: WriterException) {
            App.log.d("Could not write Barcode bitmap", throwable = e)
            null
        }
    }

    private fun writeBarcode(bitmap: Bitmap, bitMatrix: BitMatrix) {
        val minWidth = min(bitMatrix.width, bitmapWidth())
        val minHeight = min(bitMatrix.height, bitmapHeight())
        for (x in 0 until minWidth) {
            for (y in 0 until minHeight) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
    }

    @Parcelize
    private data class SavedState(
        val viewState: Parcelable?,
        val barcodeValue: String,
        val barcodeFormat: BarcodeFormat,
    ) : Parcelable
}
