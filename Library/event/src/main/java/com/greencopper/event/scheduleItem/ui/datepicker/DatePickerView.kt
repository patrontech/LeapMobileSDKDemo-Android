package com.greencopper.event.scheduleItem.ui.datepicker

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.greencopper.event.R
import com.greencopper.event.databinding.ViewDatePickerBinding
import com.greencopper.event.scheduleItem.ui.datepicker.dynamic.DynamicDatePicker
import com.greencopper.toolkit.App
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

/**
A custom view that holds a 3 types of date views: Scrollable, Finite (up to 5 dates), or a OneDay date.
This class manages and propagates the state of checks to it's child scroller or finite picker.
 */
public class DatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewDatePickerBinding.inflate(LayoutInflater.from(context), this)

    private val dynamicDatePicker: DynamicDatePicker = binding.dynamicDatePicker
    private val oneDayPicker: OneDayDatePicker = binding.oneDatePicker
    private val finitePicker: FiniteDatePicker = binding.finiteDatePicker

    private var currentMode: Mode = Mode.OneDay

    private var dates = emptyList<ZonedDateTime>()

    private val currentView: DatePickerChildView
        get() = when (currentMode) {
            Mode.OneDay -> oneDayPicker
            Mode.Finite -> finitePicker
            Mode.Scrollable -> dynamicDatePicker
        }

    public fun setDateChangeListener(dateChangeListener: DateChangeListener) {
        dynamicDatePicker.setDateChangeListener(dateChangeListener)
        finitePicker.setDateChangeListener(dateChangeListener)
        oneDayPicker.setDateChangeListener(dateChangeListener)
    }

    public fun setDates(
        newDates: List<ZonedDateTime>,
        displayMode: DisplayMode,
        selectedDate: ZonedDateTime,
    ) {
        if (newDates != dates) {
            setMode(newDates)

            val locale = App.locale
            val dateData = newDates.map {
                it.toDatePickerViewData(locale, displayMode)
            }

            currentView.setDates(dateData, selectedDate)
        } else {
            currentView.selectDate(selectedDate)
        }
    }

    private fun setMode(newDates: List<ZonedDateTime>) {
        currentMode = when {
            newDates.size == 1 -> Mode.OneDay
            newDates.size > 5 || finitePickerTooWide(newDates.size) -> Mode.Scrollable
            else -> Mode.Finite
        }

        oneDayPicker.isVisible = currentMode == Mode.OneDay
        finitePicker.isVisible = currentMode == Mode.Finite
        dynamicDatePicker.isVisible = currentMode == Mode.Scrollable
    }

    private fun finitePickerTooWide(numButtons: Int): Boolean {
        val buttonSize = context.resources.getDimensionPixelSize(R.dimen.datepickerbutton_width)
        val padding = context.resources.getDimensionPixelSize(R.dimen.datepickerbutton_horizontal_margin)
        val finitePickerWidth = (buttonSize * numButtons) + padding * 2
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        return finitePickerWidth > screenWidth
    }

    public enum class DisplayMode {
        DAILY,
        MONTHLY,
    }
}

internal interface DatePickerChildView {
    fun setDateChangeListener(listener: DateChangeListener)
    fun setDates(newDates: List<DatePickerViewData>, selectedDate: ZonedDateTime)
    fun selectDate(selectedDate: ZonedDateTime)
}

private enum class Mode {
    Scrollable, Finite, OneDay
}

private fun ZonedDateTime.toDatePickerViewData(
    locale: Locale,
    displayMode: DatePickerView.DisplayMode,
): DatePickerViewData =
    if (displayMode == DatePickerView.DisplayMode.MONTHLY) {
        DatePickerViewData(
            topLine = year.toString(),
            bottomLine = month.getDisplayName(TextStyle.SHORT, locale),
            contentDescription = "${month.getDisplayName(TextStyle.FULL, locale)} $year",
            fullDate = this,
        )
    } else {
        DatePickerViewData(
            topLine = "${month.getDisplayName(TextStyle.SHORT, locale)} $dayOfMonth",
            bottomLine = dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
            contentDescription = "${month.getDisplayName(TextStyle.FULL, locale)} $dayOfMonth",
            fullDate = this,
        )
    }
