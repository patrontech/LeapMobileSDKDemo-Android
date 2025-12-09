package com.greencopper.interfacekit.ui.views.segmentedbutton

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.SegmentedButtonBinding
import com.greencopper.interfacekit.ui.dpToPx

internal class SegmentedButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = SegmentedButtonBinding.inflate(LayoutInflater.from(context), this, true)

    private var selectedBorderColor: Int = 0
    private var selectedBackgroundColor: Int = 0
    private var selectedTextColor: Int = 0
    private var unselectedBorderColor: Int = 0
    private var unselectedBackgroundColor: Int = 0
    private var unselectedTextColor: Int = 0

    fun setup(
        text: String,
        onClick: () -> Unit,
        position: SegmentedButtonPosition,
        selectedBorderColor: Int,
        selectedBackgroundColor: Int,
        selectedTextColor: Int,
        unselectedBorderColor: Int,
        unselectedBackgroundColor: Int,
        unselectedTextColor: Int
    ) {
        binding.segmentedButtonBorder.setBackgroundResource(when (position) {
            SegmentedButtonPosition.LEFT -> R.drawable.segmented_button_border_left
            SegmentedButtonPosition.RIGHT -> R.drawable.segmented_button_border_right
            SegmentedButtonPosition.CENTER -> R.drawable.segmented_button_border_center
        })

        binding.segmentedButtonBackground.setBackgroundResource(when (position) {
            SegmentedButtonPosition.LEFT -> R.drawable.button_background_rounded_left
            SegmentedButtonPosition.RIGHT -> R.drawable.button_background_rounded_right
            SegmentedButtonPosition.CENTER -> R.drawable.button_background
        })

        this.selectedBorderColor = selectedBorderColor
        this.selectedBackgroundColor = selectedBackgroundColor
        this.selectedTextColor = selectedTextColor
        this.unselectedBorderColor = unselectedBorderColor
        this.unselectedBackgroundColor = unselectedBackgroundColor
        this.unselectedTextColor = unselectedTextColor

        binding.segmentedButtonText.text = text

        isSelected = false
        setOnSafeClickListener { onClick() }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        val backgroundDrawable = binding.segmentedButtonBackground.background as GradientDrawable
        val borderDrawable = binding.segmentedButtonBorder.background as GradientDrawable

        if (selected) {
            binding.segmentedButtonText.setTextColor(selectedTextColor)
            binding.segmentedButtonText.setTypeface(null, Typeface.BOLD)
            binding.root.elevation = 4f
            backgroundDrawable.colorFilter = PorterDuffColorFilter(selectedBackgroundColor, PorterDuff.Mode.SRC_IN)
            borderDrawable.setStroke(2.dpToPx(), selectedBorderColor)
        } else {
            binding.segmentedButtonText.setTextColor(unselectedTextColor)
            binding.segmentedButtonText.setTypeface(null, Typeface.NORMAL)
            binding.root.elevation = 0f
            backgroundDrawable.colorFilter = PorterDuffColorFilter(unselectedBackgroundColor, PorterDuff.Mode.SRC_IN)
            borderDrawable.setStroke(1.dpToPx(), unselectedBorderColor)
        }

        binding.segmentedButtonBackground.background = backgroundDrawable
        binding.segmentedButtonBorder.background = borderDrawable
    }
}

internal enum class SegmentedButtonPosition {
    LEFT, CENTER, RIGHT
}
