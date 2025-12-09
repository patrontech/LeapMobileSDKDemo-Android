package com.greencopper.event.dao

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.event.timeSlot.data.database.TimeSlotDao
import com.greencopper.event.timeSlot.data.database.TimeSlotEntity
import com.greencopper.event.timeSlot.dateTimeFormatterForTesting
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class TimeSlotDaoTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var dao: TimeSlotDao

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        val db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java).build()
        dao = db.timeSlotDao()
    }

    @Test
    fun getAll_withEmptyDb_returnsEmptyList() {
        runTest {
            val result = dao.getAllTimeSlots()
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun getAll_withInserts_returnsList() {
        val insertList = listOf(
            TimeSlotEntity(scheduleItemId = 0, dayOfEventText = generateDatePlusDaysText(0)),
            TimeSlotEntity(2, 0, generateDatePlusDaysText(0), "", ""),
            TimeSlotEntity(3, 0, generateDatePlusDaysText(0), "", "")
        )
        runTest {
            dao.insertAll(insertList)
            val result = dao.getAllTimeSlots()
            assertThat(result).hasSameSizeAs(insertList)
        }
    }

    @Test
    fun getById_withEmptyDb_returnsNull() {
        runTest {
            val result = dao.getTimeSlotById(1)
            assertThat(result).isNull()
        }
    }

    @Test
    fun getById_withInserts_returnsCorrectEntity() {
        val firstEntity = TimeSlotEntity(1, 0, generateDatePlusDaysText(1), "", "")
        val secondEntity = TimeSlotEntity(2, 0, generateDatePlusDaysText(2), "", "")
        val thirdEntity = TimeSlotEntity(3, 0, generateDatePlusDaysText(3), "", "")

        runTest {
            dao.insertAll(listOf(firstEntity, secondEntity, thirdEntity))

            val firstResult = dao.getTimeSlotById(firstEntity.id)
            assertThat(firstResult?.dayOfEventText).isEqualTo(firstEntity.dayOfEventText)

            val secondResult = dao.getTimeSlotById(secondEntity.id)
            assertThat(secondResult?.dayOfEventText).isEqualTo(secondEntity.dayOfEventText)
        }
    }

    @Test
    fun getByScheduleId_withEmptyDb_returnsEmptyList() {
        runTest {
            val result = dao.getTimeSlotsByScheduleItem(1)
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun getByScheduleId_withInserts_returnsCorrectEntities() {
        val schedule1Entities = listOf(
            TimeSlotEntity(1, 1, generateDatePlusDaysText(-1), "", ""),
            TimeSlotEntity(2, 1, generateDatePlusDaysText(-2), "", "")
        )

        val schedule2Entities = listOf(
            TimeSlotEntity(3, 2, generateDatePlusDaysText(1), "", ""),
            TimeSlotEntity(4, 2, generateDatePlusDaysText(2), "", ""),
            TimeSlotEntity(5, 2, generateDatePlusDaysText(3), "", "")
        )

        runTest {
            dao.insertAll(schedule1Entities + schedule2Entities)

            val schedule1Result = dao.getTimeSlotsByScheduleItem(schedule1Entities.first().scheduleItemId)
            assertThat(schedule1Result).hasSameSizeAs(schedule1Entities)

            val schedule2Result = dao.getTimeSlotsByScheduleItem(schedule2Entities.first().scheduleItemId)
            assertThat(schedule2Result).hasSameSizeAs(schedule2Entities)
        }
    }

    @Test
    fun getByScheduleIds_withEmptyDb_returnsEmptyList() {
        runTest {
            val result = dao.getTimeSlotsByScheduleItem(1)
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun getByScheduleIds_withInserts_returnsCorrectEntities() {
        val scheduleIds = listOf(1L, 2L)
        val schedule1Entities = listOf(
            TimeSlotEntity(1, scheduleIds[0], generateDatePlusDaysText(-1), "", ""),
            TimeSlotEntity(2, scheduleIds[0], generateDatePlusDaysText(-2), "", "")
        )

        val schedule2Entities = listOf(
            TimeSlotEntity(3, scheduleIds[1], generateDatePlusDaysText(1), "", ""),
            TimeSlotEntity(4, scheduleIds[1], generateDatePlusDaysText(2), "", ""),
            TimeSlotEntity(5, scheduleIds[1], generateDatePlusDaysText(3), "", "")
        )

        runTest {
            dao.insertAll(schedule1Entities + schedule2Entities)

            val result = dao.getTimeSlotsByScheduleItems(scheduleIds)
            assertThat(result).hasSameSizeAs(schedule1Entities + schedule2Entities)
        }
    }

    private fun generateDatePlusDaysText(days: Int): String =
        ZonedDateTime.now()
            .plusDays(days.toLong())
            .format(dateTimeFormatterForTesting)
}
