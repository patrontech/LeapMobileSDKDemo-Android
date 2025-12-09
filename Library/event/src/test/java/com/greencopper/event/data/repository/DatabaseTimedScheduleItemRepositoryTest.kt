package com.greencopper.event.data.repository

import com.greencopper.event.data.database.ScheduleItemTimeSlotStageJoined
import com.greencopper.event.mock.database.MockDatabaseHelper
import com.greencopper.event.mock.database.MockTimedScheduleItemDao
import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.scheduleItem.data.database.ScheduleItemEntity
import com.greencopper.event.stage.Stage
import com.greencopper.event.stage.data.StageEntity
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.event.timeSlot.data.database.TimeSlotEntity
import com.greencopper.event.timeSlot.dateTimeFormatter
import com.greencopper.testmocks.CoroutineTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class DatabaseTimedScheduleItemRepositoryTest : CoroutineTest() {

    private val databaseHelper = MockDatabaseHelper()
    private lateinit var timedScheduleItemDao: MockTimedScheduleItemDao

    private val repository: TimedScheduleItemRepository

    private val date = ZonedDateTime.now()

    private val scheduleItem1 = ScheduleItem(
        itemId = 11,
        activityId = 31,
        stageId = 41,
        name = "name11",
        subtitle = "subtitle11",
        description = "description11",
        photos = listOf(),
        tags = listOf("tag11"),
        performerIds = listOf("p1", "p2")
    )
    private val timeSlot1 = TimeSlot(
        id = 21,
        scheduleItemId = scheduleItem1.itemId,
        dayOfEvent = date,
        startDate = date,
        endDate = date
    )
    private val stage1 = Stage(
        id = 41,
        name = "name41",
        subtitle = "subtitle41",
        photos = listOf(),
        tags = listOf(),
        stageDetailLink = "link41",
        order = 41,
    )
    private val scheduleItemTimeSlotJoined1 = ScheduleItemTimeSlotStageJoined(
        ScheduleItemEntity(
            id = scheduleItem1.itemId,
            activityId = scheduleItem1.activityId,
            stageId = scheduleItem1.stageId,
            name = scheduleItem1.name,
            subtitle = scheduleItem1.subtitle,
            description = scheduleItem1.description,
            photos = scheduleItem1.photos,
            tags = scheduleItem1.tags,
            performerIds = scheduleItem1.performerIds,
        ),
        TimeSlotEntity(
            id = timeSlot1.id,
            scheduleItemId = scheduleItem1.itemId,
            dayOfEventText = date.format(dateTimeFormatter),
            startDateText = date.format(dateTimeFormatter),
            endDateText = date.format(dateTimeFormatter)
        ),
        StageEntity(
            id = stage1.id,
            name = stage1.name,
            subtitle = stage1.subtitle,
            photos = stage1.photos,
            tags = stage1.tags,
            stageDetailLink = stage1.stageDetailLink,
            stageOrder = stage1.order,
        )
    )

    private val scheduleItemTimeSlotJoined2 = ScheduleItemTimeSlotStageJoined(
        ScheduleItemEntity(
            id = 12,
            activityId = 32,
            stageId = 42,
            name = "name12",
            subtitle = "subtitle12",
            description = "description12",
            photos = listOf(),
            tags = listOf("tag12"),
            performerIds = listOf("p1")
        ),
        TimeSlotEntity(
            id = timeSlot1.id,
            scheduleItemId = scheduleItem1.itemId,
            dayOfEventText = date.format(dateTimeFormatter),
            startDateText = date.format(dateTimeFormatter),
            endDateText = date.format(dateTimeFormatter)
        ),
        null,
    )

    init {
        runTest {
            timedScheduleItemDao = databaseHelper.eventDatabase().first()
                .timedScheduleItemDao() as MockTimedScheduleItemDao
        }
        repository = DatabaseTimedScheduleItemRepository(
            databaseHelper, dispatcher
        )
    }

    override fun afterEach() {}

    @Test
    fun getByActivity_withExistingId_shouldReturnValue() {
        //given
        val activityId = scheduleItem1.activityId
        timedScheduleItemDao.getTimedScheduleItemsForActivityResult = {
            if (it == activityId) {
                listOf(scheduleItemTimeSlotJoined1)
            } else {
                emptyList()
            }
        }

        runTest {
            //when
            val items = repository.getTimedScheduleItemsForActivity(activityId).first()
            val result = items.first()

            //then
            assertThat(items.size).isEqualTo(1)
            assertThat(result.scheduleItem.itemId).isEqualTo(scheduleItem1.itemId)
            assertThat(result.timeSlot.id).isEqualTo(timeSlot1.id)
            assertThat(result.stage?.id).isEqualTo(stage1.id)
        }
    }

    @Test
    fun getByTags_withExistingTag_shouldBuildCorrectQuery_andReturnValue() {
        //given
        val tag = scheduleItem1.tags.first()
        timedScheduleItemDao.getTimedScheduleItemsForTagsResult = {
            val query = it.sql
            val expectedQuery =
                DatabaseTimedScheduleItemRepository.requestPrefix + " WHERE ScheduleItemEntity.tags LIKE '%\"$tag\"%'"
            if (query == expectedQuery) {
                listOf(scheduleItemTimeSlotJoined1)
            } else {
                emptyList()
            }
        }

        runTest {
            //when
            val items = repository.getTimedScheduleItemsForTags("tags LIKE '%\"$tag\"%'").first()
            val result = items.first()

            //then
            assertThat(items.size).isEqualTo(1)
            assertThat(result.scheduleItem.itemId).isEqualTo(scheduleItem1.itemId)
            assertThat(result.timeSlot.id).isEqualTo(timeSlot1.id)
            assertThat(result.stage?.id).isEqualTo(stage1.id)
        }
    }

    @Test
    fun getByTags_withNoQuery_shouldReturnValue() {
        //given
        timedScheduleItemDao.getTimedScheduleItemsForTagsResult = {
            val query = it.sql
            val expectedQuery =
                DatabaseTimedScheduleItemRepository.requestPrefix
            if (query == expectedQuery) {
                listOf(scheduleItemTimeSlotJoined1, scheduleItemTimeSlotJoined2)
            } else {
                emptyList()
            }
        }

        runTest {
            //when
            val items = repository.getTimedScheduleItemsForTags(null).first()
            val result = items.first()

            //then
            assertThat(items.size).isEqualTo(2)
            assertThat(result.scheduleItem.itemId).isEqualTo(scheduleItem1.itemId)
            assertThat(result.timeSlot.id).isEqualTo(timeSlot1.id)
            assertThat(result.stage?.id).isEqualTo(stage1.id)
        }
    }

    @Test
    fun getByScheduleId_withExistingId_shouldReturnValue() {
        //given
        val scheduleIds = listOf(scheduleItem1.itemId)
        timedScheduleItemDao.getTimedScheduleItemsForScheduleIdsResult = {
            if (it == scheduleIds) {
                listOf(scheduleItemTimeSlotJoined1)
            } else {
                emptyList()
            }
        }

        runTest {
            //when
            val items = repository.getTimedScheduleItemsForScheduleItemIds(scheduleIds).first()
            val result = items.first()

            //then
            assertThat(items.size).isEqualTo(1)
            assertThat(result.scheduleItem.itemId).isEqualTo(scheduleItem1.itemId)
            assertThat(result.timeSlot.id).isEqualTo(timeSlot1.id)
            assertThat(result.stage?.id).isEqualTo(stage1.id)
        }
    }

    @Test
    fun getByPerformerId_withExistingId_shouldReturnValue() {
        //given
        val performerId = "p1"
        timedScheduleItemDao.getTimedScheduleItemsForPerformerResult = {
            if (it == "%\"$performerId\"%") {
                listOf(scheduleItemTimeSlotJoined1)
            } else {
                emptyList()
            }
        }

        runTest {
            //when
            val items = repository.getTimedScheduleItemsForPerformer(performerId).first()
            val result = items.first()

            //then
            assertThat(items.size).isEqualTo(1)
            assertThat(result.scheduleItem.itemId).isEqualTo(scheduleItem1.itemId)
            assertThat(result.timeSlot.id).isEqualTo(timeSlot1.id)
            assertThat(result.stage?.id).isEqualTo(stage1.id)
        }
    }
}
