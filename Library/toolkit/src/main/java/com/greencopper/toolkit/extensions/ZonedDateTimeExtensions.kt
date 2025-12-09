package com.greencopper.toolkit.extensions

import com.greencopper.toolkit.App
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.IsoChronology
import java.time.format.*
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

/**
 * Get formatted date time of given ZonedDateTime
 * @return Date formatted according to the current Locale and given formats
 * @throws IllegalArgumentException if both formats are null
 */
@Throws(IllegalArgumentException::class)
public fun ZonedDateTime.getFormattedDateTime(
    dateFormat: FormatStyle?,
    timeFormat: FormatStyle?,
    zoneId: ZoneId
): String {
    val pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
        dateFormat,
        timeFormat,
        IsoChronology.INSTANCE,
        App.locale
    )
    return withZoneSameInstant(zoneId).format(DateTimeFormatter.ofPattern(pattern))
}

public fun ZonedDateTime.isSameDayAs(zonedDateTime: ZonedDateTime): Boolean =
    this.toLocalDate().isEqual(zonedDateTime.toLocalDate())

public fun ZonedDateTime.isYesterdayFor(zonedDateTime: ZonedDateTime): Boolean =
    this.toLocalDate().isEqual(zonedDateTime.minusDays(1).toLocalDate())

public fun ZonedDateTime.truncateToMonth(): ZonedDateTime =
    this.truncatedTo(ChronoUnit.DAYS).with(ChronoField.DAY_OF_MONTH, 1)
