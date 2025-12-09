package com.greencopper.event.timeSlot

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class TimeSlotTest {

    @Test
    fun testComparator() {
        val now = ZonedDateTime.now()

        val t1 = TimeSlot(
            id = 1,
            scheduleItemId = 1,
            dayOfEvent = now,
            startDate = now,
            endDate = now
        )

        assertThat(
            t1.compareTo(
                TimeSlot(
                    id = 2,
                    scheduleItemId = 2,
                    dayOfEvent = now,
                    startDate = now,
                    endDate = now
                )
            )
        ).isEqualTo(0)

        assertThat(
            t1.compareTo(
                TimeSlot(
                    id = 2,
                    scheduleItemId = 2,
                    dayOfEvent = now.plusMinutes(1),
                    startDate = now,
                    endDate = now
                )
            )
        ).isEqualTo(-1)

        assertThat(
            t1.compareTo(
                TimeSlot(
                    id = 2,
                    scheduleItemId = 2,
                    dayOfEvent = now,
                    startDate = now.plusMinutes(1),
                    endDate = now
                )
            )
        ).isEqualTo(-1)

        assertThat(
            t1.compareTo(
                TimeSlot(
                    id = 2,
                    scheduleItemId = 2,
                    dayOfEvent = now,
                    startDate = now,
                    endDate = now.minusMinutes(1)
                )
            )
        ).isEqualTo(1)

        assertThat(
            t1.compareTo(
                TimeSlot(
                    id = 2,
                    scheduleItemId = 2,
                    dayOfEvent = now,
                    startDate = null,
                    endDate = now.plusMinutes(1)
                )
            )
        ).isEqualTo(1)

        assertThat(
            t1.compareTo(
                TimeSlot(
                    id = 2,
                    scheduleItemId = 2,
                    dayOfEvent = now,
                    startDate = now,
                    endDate = null
                )
            )
        ).isEqualTo(1)

        assertThat(
            TimeSlot(
                id = 2,
                scheduleItemId = 2,
                dayOfEvent = now,
                startDate = null,
                endDate = now.plusMinutes(1)
            ).compareTo(t1)
        ).isEqualTo(-1)

        assertThat(
            TimeSlot(
                id = 2,
                scheduleItemId = 2,
                dayOfEvent = now,
                startDate = now,
                endDate = null
            ).compareTo(t1)
        ).isEqualTo(-1)
    }

}
