package com.greencopper.event.dao

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.event.scheduleItem.data.database.ScheduleItemDao
import com.greencopper.event.scheduleItem.data.database.ScheduleItemEntity
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ScheduleItemDaoTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var dao: ScheduleItemDao

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        val db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java).build()
        dao = db.scheduleItemDao()
    }

    @Test
    fun getAll_withEmptyDb_returnsEmptyList() {
        runTest {
            val result = dao.getAllScheduleItems()
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun getAll_withInserts_returnsList() {
        val insertList = listOf(
            ScheduleItemEntity(activityId = 0, name = "", photos = listOf()),
            ScheduleItemEntity(2, 0, 0, "", "", "", listOf()),
            ScheduleItemEntity(3, 0, 0, "", "", "", listOf())
        )

        runTest {
            dao.insertAll(insertList)

            val result = dao.getAllScheduleItems()
            assertThat(result).hasSameSizeAs(insertList)
        }
    }

    @Test
    fun getById_withEmptyDb_returnsNull() {
        runTest {
            val result = dao.getScheduleItemById(1)
            assertThat(result).isNull()
        }
    }

    @Test
    fun getById_withInserts_returnsCorrectEntity() {
        val firstEntity = ScheduleItemEntity(1, 0, 0, "first", "", "", listOf())
        val secondEntity = ScheduleItemEntity(2, 0, 0, "second", "", "", listOf())
        val thirdEntity = ScheduleItemEntity(3, 0, 0, "third", "", "", listOf())

        runTest {
            dao.insertAll(listOf(firstEntity, secondEntity, thirdEntity))

            val firstResult = dao.getScheduleItemById(firstEntity.id)
            assertThat(firstResult?.name).isEqualTo(firstEntity.name)

            val thirdResult = dao.getScheduleItemById(thirdEntity.id)
            assertThat(thirdResult?.name).isEqualTo(thirdEntity.name)
        }
    }

    @Test
    fun getByActivity_withEmptyDb_returnsEmptyList() {
        runTest {
            val result = dao.getScheduleItemsForActivity(1)
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun getByActivityId_withInserts_returnsCorrectEntities() {
        val activity1Entities = listOf(
            ScheduleItemEntity(1, 1, 0, "first1", "", "", listOf()),
            ScheduleItemEntity(2, 1, 0, "second1", "", "", listOf())
        )
        val activity2Entities = listOf(
            ScheduleItemEntity(3, 2, 0, "first2", "", "", listOf()),
            ScheduleItemEntity(4, 2, 0, "second2", "", "", listOf()),
            ScheduleItemEntity(5, 2, 0, "third2", "", "", listOf())
        )

        runTest {
            dao.insertAll(activity1Entities + activity2Entities)

            val activity1Result = dao.getScheduleItemsForActivity(activity1Entities.first().activityId)
            assertThat(activity1Result).hasSameSizeAs(activity1Entities)

            val activity2Result = dao.getScheduleItemsForActivity(activity2Entities.first().activityId)
            assertThat(activity2Result).hasSameSizeAs(activity2Entities)
        }
    }
}
