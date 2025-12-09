package com.greencopper.event.scheduleItem.ui.datepicker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.DateOneDayBinding
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.extensions.getFormattedDateTime
import java.time.ZonedDateTime
import java.time.format.FormatStyle

internal class OneDayDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), DatePickerChildView {

    private val binding = DateOneDayBinding.inflate(LayoutInflater.from(context), this, true)
    private val timezoneProvider: TimezoneProvider = App.resolve()

    init {
        binding.root.setBackgroundColor(EventColor.schedule.oneDay.background)
        binding.singleDateTv.setTextColor(EventColor.schedule.oneDay.label)
        binding.singleDateTv.setFont(EventTextStyle.schedule.header.oneDay.label)
    }

    override fun setDates(newDates: List<DatePickerViewData>, selectedDate: ZonedDateTime) {
        if (newDates.isNotEmpty()) {
            binding.singleDateTv.text = newDates[0].fullDate.getFormattedDateTime(
                dateFormat = FormatStyle.FULL,
                timeFormat = null,
                zoneId = timezoneProvider.zoneId
            )
        }
    }

    override fun selectDate(selectedDate: ZonedDateTime) {
        // Unused: Can't change date in this view
    }

    override fun setDateChangeListener(listener: DateChangeListener) {
        // Unused: Can't change date in this view
    }
}
