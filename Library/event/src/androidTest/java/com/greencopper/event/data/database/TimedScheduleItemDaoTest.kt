package com.greencopper.event.data.database

import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.event.dao.EventDatabase
import com.greencopper.event.data.repository.DatabaseTimedScheduleItemRepository
import com.greencopper.event.scheduleItem.data.database.ScheduleItemDao
import com.greencopper.event.scheduleItem.data.database.ScheduleItemEntity
import com.greencopper.event.stage.data.StageDao
import com.greencopper.event.stage.data.StageEntity
import com.greencopper.event.timeSlot.data.database.TimeSlotDao
import com.greencopper.event.timeSlot.data.database.TimeSlotEntity
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TimedScheduleItemDaoTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var stageDao: StageDao
    private lateinit var timeSlotDao: TimeSlotDao
    private lateinit var scheduleItemDao: ScheduleItemDao
    private lateinit var timedScheduleItemDao: TimedScheduleItemDao

    private val scheduleItem1 = ScheduleItemEntity(
        id = 11,
        activityId = 31,
        stageId = 41,
        name = "name11",
        subtitle = "subtitle11",
        description = "description11",
        photos = listOf(),
        tags = listOf("tag11")
    )
    private val scheduleItem2 = ScheduleItemEntity(
        id = 12,
        activityId = 32,
        stageId = 42,
        name = "name12",
        subtitle = "subtitle12",
        description = "description12",
        photos = listOf(),
        tags = listOf("tag12")
    )
    private val timeSlot1 = TimeSlotEntity(
        id = 21,
        scheduleItemId = scheduleItem1.id,
        dayOfEventText = "day21",
        startDateText = "startDate21",
        endDateText = "endDate21"
    )
    private val timeSlot2 = TimeSlotEntity(
        id = 22,
        scheduleItemId = scheduleItem2.id,
        dayOfEventText = "day22",
        startDateText = "startDate22",
        endDateText = "endDate22"
    )

    private val stage1 = StageEntity(
        id = 41,
        name = "name41",
        subtitle = "subtitle41",
        photos = listOf(),
        tags = listOf(),
        stageDetailLink = "link41",
        stageOrder = 41
    )

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        val db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java).build()
        stageDao = db.stageDao()
        timeSlotDao = db.timeSlotDao()
        scheduleItemDao = db.scheduleItemDao()
        timedScheduleItemDao = db.timedScheduleItemDao()

        scheduleItemDao.insertAll(listOf(scheduleItem1, scheduleItem2))
        timeSlotDao.insertAll(listOf(timeSlot1, timeSlot2))
        stageDao.insertAll(listOf(stage1))
    }

    @Test
    fun getByActivity_withExistingId_shouldReturnValue() {
        //when
        val items = timedScheduleItemDao.getTimedScheduleItemsForActivity(32)
        val result = items.first()

        //then
        assertThat(items.size).isEqualTo(1)
        assertThat(result.scheduleItem.id).isEqualTo(12)
        assertThat(result.timeSlot.id).isEqualTo(22)
        assertThat(result.stage).isNull()
    }

    @Test
    fun getByActivity_withNonExistingId_shouldReturnEmpty() {
        //when
        val items = timedScheduleItemDao.getTimedScheduleItemsForActivity(100)

        //then
        assertThat(items).isEmpty()
    }

    @Test
    fun getByTags_withExistingTag_shouldReturnValue() {
        //given
        val sqlRequest =
            SimpleSQLiteQuery(DatabaseTimedScheduleItemRepository.requestPrefix + " WHERE ScheduleItemEntity.tags LIKE '%\"tag12\"%'")

        //when
        val items = timedScheduleItemDao.getTimedScheduleItemsForTags(sqlRequest)
        val result = items.first()

        //then
        assertThat(items.size).isEqualTo(1)
        assertThat(result.scheduleItem.id).isEqualTo(12)
        assertThat(result.timeSlot.id).isEqualTo(22)
    }

    @Test
    fun getByTags_withNonExistingTag_shouldReturnEmpty() {
        //given
        val sqlRequest =
            SimpleSQLiteQuery(DatabaseTimedScheduleItemRepository.requestPrefix + " WHERE ScheduleItemEntity.tags LIKE '%\"test\"%'")

        //when
        val items = timedScheduleItemDao.getTimedScheduleItemsForTags(sqlRequest)

        //then
        assertThat(items).isEmpty()
    }

    @Test
    fun getByScheduleId_withExistingTag_shouldReturnValue() {
        //when
        val items = timedScheduleItemDao.getTimedScheduleItemsForScheduleIds(listOf(11))
        val result = items.first()

        //then
        assertThat(items.size).isEqualTo(1)
        assertThat(result.scheduleItem.id).isEqualTo(11)
        assertThat(result.timeSlot.id).isEqualTo(21)
        assertThat(result.stage?.id).isEqualTo(41)
    }

    @Test
    fun getByScheduleId_withNonExistingId_shouldReturnEmpty() {
        //when
        val items = timedScheduleItemDao.getTimedScheduleItemsForScheduleIds(listOf(100))

        //then
        assertThat(items).isEmpty()
    }

    @Test
    fun getByScheduleId_withEmptyList_shouldReturnEmpty() {
        //when
        val items = timedScheduleItemDao.getTimedScheduleItemsForScheduleIds(listOf())

        //then
        assertThat(items).isEmpty()
    }
}
