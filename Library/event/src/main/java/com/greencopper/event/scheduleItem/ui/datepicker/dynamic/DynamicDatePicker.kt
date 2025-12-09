package com.greencopper.event.scheduleItem.ui.datepicker.dynamic

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.postDelayed
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.DatePickerDynamicBinding
import com.greencopper.event.scheduleItem.ui.datepicker.*
import java.time.ZonedDateTime

internal class DynamicDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), DatePickerChildView {

    private val binding = DatePickerDynamicBinding.inflate(LayoutInflater.from(context), this)
    private var dateAdapter: DatePickerAdapter = DatePickerAdapter()
    private val datePicker = binding.scrollingDatepickerRv

    init {
        setBackgroundColor(EventColor.schedule.datepicker.background)

        datePicker.adapter = dateAdapter
    }

    override fun setDateChangeListener(listener: DateChangeListener) {
        dateAdapter.setDateChangeListener { selectedDate, position ->
            listener.onDateChanged(selectedDate)
            datePicker.postDelayed(100) {
                datePicker.scrollToPosition(position)
            }
        }
    }

    override fun setDates(newDates: List<DatePickerViewData>, selectedDate: ZonedDateTime) {
        dateAdapter.setOnCurrentListChanged {
            selectDate(selectedDate)
        }
        dateAdapter.setDates(newDates)
    }

    override fun selectDate(selectedDate: ZonedDateTime) {
        val position = dateAdapter.currentList.indexOfFirst {
            it.fullDate == selectedDate
        }
        dateAdapter.selectDate(position)
        datePicker.postDelayed(100) {
            datePicker.setDynamicDecorators()
            datePicker.scrollToPosition(position)
        }
    }
}
