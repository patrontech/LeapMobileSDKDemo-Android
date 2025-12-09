package com.greencopper.interfacekit.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.greencopper.interfacekit.R

public class DotsIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
    }

    public fun setup(
        numberOfDots: Int,
        currentPosition: Int,
        @DimenRes dotSize: Int = R.dimen.dots_indicator_dot_size,
        @ColorInt selectedDotColor: Int,
        @ColorInt defaultDotColor: Int
    ) {
        if (currentPosition > numberOfDots) {
            throw IllegalArgumentException("current position $currentPosition can't be greater than number of dots $numberOfDots")
        }

        removeAllViews()
        val defaultDotDrawable =
            ContextCompat.getDrawable(context, R.drawable.dot_indicator_drawable)
                ?.apply { setTint(defaultDotColor) }
        val selectedDotDrawable =
            ContextCompat.getDrawable(context, R.drawable.dot_indicator_drawable)
                ?.apply { setTint(selectedDotColor) }
        for (i in 0 until numberOfDots) {
            val dotImageView = ImageView(context)
            val layoutParams = LayoutParams(
                resources.getDimension(dotSize).toInt(),
                resources.getDimension(dotSize).toInt()
            )
            val margin = resources.getDimension(R.dimen.dots_indicator_dot_margin).toInt()
            layoutParams.setMargins(margin, 0, margin, 0)
            dotImageView.layoutParams = layoutParams

            if (i == currentPosition) {
                dotImageView.setImageDrawable(selectedDotDrawable)
            } else {
                dotImageView.setImageDrawable(defaultDotDrawable)
            }
            addView(dotImageView)
        }
    }
}

@Composable
public fun DotsIndicator(
    numberOfDots: Int,
    selectedPosition: Int,
    selectedDotColor: Color,
    defaultDotColor: Color,
    modifier: Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        for (i in 0 until numberOfDots) {
            Canvas(
                onDraw = { drawCircle(color = if (i == selectedPosition) selectedDotColor else defaultDotColor) },
                modifier = Modifier
                .padding(2.dp, 0.dp, 2.dp, 0.dp)
                .size(8.dp)
            )
        }
    }
}
