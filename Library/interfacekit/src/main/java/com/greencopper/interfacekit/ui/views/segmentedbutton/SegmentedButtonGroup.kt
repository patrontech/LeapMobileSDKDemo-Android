package com.greencopper.interfacekit.ui.views.segmentedbutton

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.greencopper.interfacekit.ui.dpToPx

public class SegmentedButtonGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val buttonGroup: ArrayList<SegmentedButton> = arrayListOf()

    public fun setup(
        buttonData: List<SegmentedButtonData>,
        backgroundColor: Int,
        selectedBorderColor: Int,
        selectedBackgroundColor: Int,
        selectedTextColor: Int,
        unselectedBorderColor: Int,
        unselectedBackgroundColor: Int,
        unselectedTextColor: Int,
        multiSelectEnabled: Boolean,
        defaultSelectedIndex: Int? = null
    ) {
        removeAllViews()
        setBackgroundColor(backgroundColor)

        buttonGroup.clear()
        buttonGroup.addAll(buttonData.mapIndexed { index, data ->
            val toggleButton = SegmentedButton(context)

            val position = when (index) {
                0 -> SegmentedButtonPosition.LEFT
                buttonData.lastIndex -> SegmentedButtonPosition.RIGHT
                else -> SegmentedButtonPosition.CENTER
            }

            toggleButton.setup(
                text = data.text,
                onClick = { onButtonClick(toggleButton, data.onEnabled, multiSelectEnabled) },
                position = position,
                selectedBorderColor = selectedBorderColor,
                selectedBackgroundColor = selectedBackgroundColor,
                selectedTextColor = selectedTextColor,
                unselectedBorderColor = unselectedBorderColor,
                unselectedBackgroundColor = unselectedBackgroundColor,
                unselectedTextColor = unselectedTextColor
            )

            if (index == defaultSelectedIndex) {
                toggleButton.isSelected = true
            }

            val params = LayoutParams(0, LayoutParams.MATCH_PARENT)
            params.weight = 1f
            toggleButton.layoutParams = params
            toggleButton.translationX -= index.dpToPx()

            toggleButton
        })

        weightSum = buttonData.size.toFloat()
        buttonGroup.forEach { addView(it) }
    }

    private fun onButtonClick(
        button: SegmentedButton,
        onEnabled: () -> Unit,
        multiSelectEnabled: Boolean
    ) {
        // if multiselect is not enabled and this button is selected, we don't change the selection
        if (!multiSelectEnabled && button.isSelected) {
            return
        }

        button.isSelected = !button.isSelected

        if (button.isSelected) {
            onEnabled()
        }

        if (!multiSelectEnabled) {
            buttonGroup.forEach {
                if (button != it) {
                    it.isSelected = false
                }
            }
        }
    }
}

public class SegmentedButtonData(
    public val text: String,
    public val onEnabled: () -> Unit
)
