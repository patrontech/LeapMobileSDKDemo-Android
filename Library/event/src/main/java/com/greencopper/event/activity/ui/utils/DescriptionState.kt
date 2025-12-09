package com.greencopper.event.activity.ui.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import com.greencopper.event.R
import com.greencopper.event.colors.EventColor

public enum class DescriptionState(
    public val textId: String? = null,
    public val arrowDrawableId: Int = 0,
    public val foregroundDrawable: Drawable? = null,
    public val activated: Boolean = true
) {
    NoOverflow(activated = false),
    Contracted(
        "common.show_more",
        R.drawable.ic_baseline_keyboard_arrow_down_24,
        GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(
                EventColor.activityDetail.background,
                Color.TRANSPARENT
            )
        )
    ),
    Expanded("common.show_less", R.drawable.ic_baseline_keyboard_arrow_up_24)
}
