package com.greencopper.event.scheduleItem.ui.utils

import com.greencopper.toolkit.extensions.isSameDayAs
import java.time.ZonedDateTime

public object DateUtils {

    /**
     * Tries to find the closest date to today inside the list.
     * @return Either today or nearest future date if today is not found in the list. If none of those are available, returns the last item of the list. If list is empty, returns today by default.
     */
    public fun getTodayOrFutureNearestDate(dateList: List<ZonedDateTime>): ZonedDateTime {
        val currentDate = ZonedDateTime.now()
        val todayOrNearest = dateList.firstOrNull { currentDate.isSameDayAs(it) } ?: run {
            val sortedDates = dateList.sorted()
            sortedDates.firstOrNull {
                it.isAfter(currentDate)
            }
        }

        // Pick today, nearest future date, or last date
        return todayOrNearest ?: dateList.lastOrNull() ?: currentDate
    }

    public fun findNearestDateOrToday(dateList: List<ZonedDateTime>, selectedDate: ZonedDateTime?): ZonedDateTime {
        val sortedDates = dateList.sorted()
        return selectedDate?.let {
            if (sortedDates.contains(selectedDate)) {
                selectedDate
            } else {
                val nextIndex = sortedDates.indexOfFirst { it.isAfter(selectedDate) }
                val previousDate = sortedDates.getOrNull(nextIndex - 1)
                val nextDate = sortedDates.getOrNull(nextIndex)

                previousDate ?: return@let nextDate
                nextDate ?: return@let previousDate

                val selectedDateDays = selectedDate.toLocalDate().toEpochDay()
                val previousDateDiff = selectedDateDays - previousDate.toLocalDate().toEpochDay()
                val nextDateDiff = nextDate.toLocalDate().toEpochDay() - selectedDateDays

                when {
                    previousDateDiff > nextDateDiff -> nextDate
                    else -> previousDate
                }
            }
        } ?: getTodayOrFutureNearestDate(dateList)
    }

}

public fun Number.minutesToSeconds(): Long = toLong() * 60

public fun ZonedDateTime.isBeforeOrEqualTo(other: ZonedDateTime): Boolean =
    isBefore(other) || this == other

public fun ZonedDateTime.isAfterOrEqualTo(other: ZonedDateTime): Boolean =
    isAfter(other) || this == other
