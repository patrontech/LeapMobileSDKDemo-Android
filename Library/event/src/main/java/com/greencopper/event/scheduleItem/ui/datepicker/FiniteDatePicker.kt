package com.greencopper.event.scheduleItem.ui.datepicker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.DatePickerFiniteBinding
import com.greencopper.interfacekit.common.setOnSafeClickListener
import java.time.ZonedDateTime

internal class FiniteDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), DatePickerChildView {

    private val binding = DatePickerFiniteBinding.inflate(LayoutInflater.from(context), this, true)
    private var dateChangeListener: DateChangeListener? = null
    private var dates: List<DatePickerViewData> = emptyList()

    init {
        binding.root.setBackgroundColor(EventColor.schedule.datepicker.background)
    }

    override fun setDateChangeListener(listener: DateChangeListener) {
        dateChangeListener = listener
    }

    override fun setDates(newDates: List<DatePickerViewData>, selectedDate: ZonedDateTime) {
        dates = newDates

        val buttons = binding.root.children.filterIsInstance<DatePickerButton>().toList()
        buttons.forEach { it.visibility = View.GONE }
        dates.mapIndexed { index, dateData ->
            if (index < buttons.size) {
                val datePickerButton = buttons[index]
                datePickerButton.setDate(dateData)
                datePickerButton.visibility = View.VISIBLE
                datePickerButton.isChecked = false

                datePickerButton.setOnSafeClickListener {
                    selectDate(dateData.fullDate)
                    dateChangeListener?.onDateChanged(dateData.fullDate)
                }
            }
        }
        selectDate(selectedDate)
    }

    override fun selectDate(selectedDate: ZonedDateTime) {
        val buttons = binding.root.children.filterIsInstance<DatePickerButton>().toList()
        buttons.mapIndexed { index, button ->
            button.isChecked = dates.getOrNull(index)?.let {
                it.fullDate == selectedDate
            } ?: false
        }
    }
}
