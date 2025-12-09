package com.greencopper.event.scheduleItem.ui.datepicker

import java.time.ZonedDateTime

public interface DateChangeListener {
    public fun onDateChanged(pickedDate: ZonedDateTime)
}