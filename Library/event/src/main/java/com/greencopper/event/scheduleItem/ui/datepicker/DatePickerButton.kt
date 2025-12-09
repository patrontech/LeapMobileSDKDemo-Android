package com.greencopper.event.scheduleItem.ui.datepicker

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.Checkable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.children
import com.greencopper.event.R
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.DatepickerButtonBinding
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.color.getCheckableColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.textstyle.subsystem.setFont

public class DatePickerButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr), Checkable {

    private val binding: DatepickerButtonBinding =
        DatepickerButtonBinding.inflate(LayoutInflater.from(context), this, true)

    private var checked: Boolean = false

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.DatePickerButton, 0, 0).use {
            isEnabled = it.getBoolean(R.styleable.DatePickerButton_android_enabled, isEnabled)
        }
        val textColor = EventColor.schedule.datepicker.label.toColorStateList()

        with(binding) {
            topLine.setTextColor(textColor)
            bottomLine.setTextColor(textColor)
            pickerBackground.setCardBackgroundColor(
                getCheckableColor(
                    EventColor.schedule.datepicker.background,
                    EventColor.schedule.datepicker.selectedItem.background
                )
            )
            pickerBackground.setOnSafeClickListener { this@DatePickerButton.performClick() }
        }
        setTextStyles()

        isClickable = true
        isFocusable = true
    }

    public fun setDate(dateViewData: DatePickerViewData) {
        binding.apply {
            topLine.text = dateViewData.topLine
            bottomLine.text = dateViewData.bottomLine
            root.contentDescription = dateViewData.contentDescription
        }
    }

    override fun setChecked(checked: Boolean) {
        checkChildren(checked)

        if (this.checked != checked) {
            this.checked = checked
            refreshDrawableState()
        }
    }

    override fun isChecked(): Boolean = checked

    override fun toggle() {
        isChecked = !checked
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        setTextStyles()
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            View.mergeDrawableStates(drawableState, CHECKED_STATE_SET)
        }
        return drawableState
    }

    private fun setTextStyles() {
        val textStyle = EventTextStyle.schedule.header.datePicker

        with(binding) {
            if (checked) {
                topLine.setFont(textStyle.selected.topLine)
                bottomLine.setFont(textStyle.selected.bottomLine)
            } else {
                topLine.setFont(textStyle.normal.topLine)
                bottomLine.setFont(textStyle.normal.bottomLine)
            }
        }
    }

    private fun ViewGroup.checkChildren(checked: Boolean) {
        this.children.filterIsInstance<Checkable>().forEach { it.isChecked = checked }
        this.children.filterIsInstance<ViewGroup>().forEach { it.checkChildren(checked) }
    }

    private companion object {
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    }
}
