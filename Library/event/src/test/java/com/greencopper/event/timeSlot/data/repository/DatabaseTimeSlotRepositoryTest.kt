package com.greencopper.event.timeSlot.data.repository

import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.dao.EventDatabase
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.event.timeSlot.data.database.TimeSlotEntity
import com.greencopper.event.timeSlot.dateTimeFormatterForTesting
import com.greencopper.testmocks.CoroutineTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime

internal class DatabaseTimeSlotRepositoryTest : CoroutineTest() {

    private val mockEventDatabase: EventDatabase = mockk()
    private val mockDatabaseHelper: DatabaseHelper = object : DatabaseHelper {
        override fun eventDatabase(): Flow<EventDatabase> = flowOf(mockEventDatabase)
    }
    private val classUnderTest = DatabaseTimeSlotRepository(mockDatabaseHelper, dispatcher)

    private val element = TimeSlot(
        id = 1,
        scheduleItemId = 1,
        dayOfEvent = ZonedDateTime.now(),
        startDate = ZonedDateTime.now(),
        endDate = ZonedDateTime.now()
    )

    override fun afterEach() {}

    @Test
    @DisplayName("Given an item is found in database, When calling getTimeSlots, Then this item is returned")
    fun getTimeSlotsShouldSucceed() {
        every { mockEventDatabase.timeSlotDao().getAllTimeSlots() } returns
                listOf(
                    TimeSlotEntity(
                        element.id,
                        element.scheduleItemId,
                        element.dayOfEvent.format(dateTimeFormatterForTesting),
                        element.startDate?.format(dateTimeFormatterForTesting),
                        element.endDate?.format(dateTimeFormatterForTesting)
                    )
                )

        runTest {
            val result = classUnderTest.getTimeSlots().first()
            assertThat(result.first().id).isEqualTo(element.id)
        }
    }

    @Test
    @DisplayName("Given an exception is thrown from database, When calling getTimeSlots, Then this exception should be thrown")
    fun getTimeSlotsShouldFail() {
        every {
            mockEventDatabase.timeSlotDao().getAllTimeSlots()
        } throws IllegalStateException()

        runTest {
            assertThrows<IllegalStateException> {
                classUnderTest.getTimeSlots().first()
            }
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getTimeSlotById, Then this item is returned")
    fun getTimeSlotByIdShouldSucceed() {
        every {
            mockEventDatabase.timeSlotDao().getTimeSlotById(element.id)
        } returns TimeSlotEntity(
            element.id,
            element.scheduleItemId,
            element.dayOfEvent.format(dateTimeFormatterForTesting),
            element.startDate?.format(dateTimeFormatterForTesting),
            element.endDate?.format(dateTimeFormatterForTesting)
        )

        runTest {
            val result = classUnderTest.getTimeSlotById(element.id).firstOrNull()
            assertThat(result?.id).isEqualTo(element.id)
        }
    }

    @Test
    @DisplayName("Given an exception is thrown from database, When calling getTimeSlotById, Then this exception should be thrown")
    fun getTimeSlotByIdShouldFail() {
        every {
            mockEventDatabase.timeSlotDao().getTimeSlotById(element.id)
        } throws IllegalArgumentException()

        runTest {
            assertThrows<IllegalArgumentException> {
                classUnderTest.getTimeSlotById(element.id).firstOrNull()
            }
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getTimeSlotForScheduleItem, Then this item is returned")
    fun getTimeSlotForScheduleItemShouldSucceed() {
        every {
            mockEventDatabase.timeSlotDao().getTimeSlotsByScheduleItem(element.scheduleItemId)
        } returns listOf(
            TimeSlotEntity(
                element.id,
                element.scheduleItemId,
                element.dayOfEvent.format(dateTimeFormatterForTesting),
                element.startDate?.format(dateTimeFormatterForTesting),
                element.endDate?.format(dateTimeFormatterForTesting)
            )
        )

        runTest {
            val result = classUnderTest.getTimeSlotForScheduleItem(element.scheduleItemId).firstOrNull()
            assertThat(result?.id).isEqualTo(element.id)
        }
    }

    @Test
    @DisplayName("Given an exception is thrown from database, When calling getTimeSlotForScheduleItem, Then this exception should be thrown")
    fun getTimeSlotForScheduleItemShouldFail() {
        every {
            mockEventDatabase.timeSlotDao().getTimeSlotsByScheduleItem(element.scheduleItemId)
        } throws RuntimeException()


        runTest {
            assertThrows<RuntimeException> {
                classUnderTest.getTimeSlotForScheduleItem(element.scheduleItemId).firstOrNull()
            }
        }
    }
}
