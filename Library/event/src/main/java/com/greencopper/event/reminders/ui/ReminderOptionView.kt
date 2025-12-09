package com.greencopper.event.reminders.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RadioGroup
import com.google.android.material.radiobutton.MaterialRadioButton
import com.greencopper.event.colors.EventColor
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.color.getCheckableColor
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.dpToPx

internal class ReminderOptionView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    MaterialRadioButton(context, attrs) {

    private val colors = EventColor.schedule.reminders.option
    private val textStyles = EventTextStyle.scheduleReminders
    private val checkedColor = colors.radioButton
    private val uncheckedColor = colors.radioButtonBorder
    internal lateinit var interval: ReminderIntervalViewData

    init {
        id = View.generateViewId()
        setTextColor(colors.text)
        setFont(textStyles.option)

        layoutParams = RadioGroup.LayoutParams(context, attrs).apply {
            val padding = 12.dpToPx()
            setPadding(padding, padding, padding, padding)
        }
    }

    fun setup(
        interval: ReminderIntervalViewData,
    ) {
        this.interval = interval
        text = interval.text
        buttonTintList = getCheckableColor(uncheckedColor, checkedColor)
    }

}

internal data class ReminderIntervalViewData(
    val minutes: Int,
    val text: String,
)
