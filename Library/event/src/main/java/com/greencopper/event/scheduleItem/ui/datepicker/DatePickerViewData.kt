package com.greencopper.event.scheduleItem.ui.datepicker

import java.time.ZonedDateTime

public data class DatePickerViewData(
    val topLine: String,
    val bottomLine: String,
    val contentDescription: String,
    val fullDate: ZonedDateTime,
)
