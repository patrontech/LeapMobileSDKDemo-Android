package com.greencopper.event.scheduleItem.data.repository

import androidx.sqlite.db.SupportSQLiteQuery
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.dao.EventDatabase
import com.greencopper.event.scheduleItem.data.database.ScheduleItemDao
import com.greencopper.event.scheduleItem.data.database.ScheduleItemEntity
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.testmocks.CoroutineTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DatabaseScheduleItemRepositoryTest : CoroutineTest() {

    private val elements = listOf(
        ScheduleItemEntity(
            id = 1,
            activityId = 1,
            stageId = 1,
            name = "name1",
            subtitle = "subtitle1",
            description = "description1",
            photos = listOf("photos1"),
            tags = listOf("tag1", "pouet"),
            performerIds = listOf("p1", "p3"),
        ),
        ScheduleItemEntity(
            id = 2,
            activityId = 2,
            stageId = 2,
            name = "name2",
            subtitle = "subtitle2",
            description = "description2",
            photos = listOf("photos2"),
            tags = listOf("tag1", "pouet"),
            performerIds = listOf("p2", "p3"),
        )
    )


    private val mockEventDatabase: EventDatabase = mockk()
    private val mockDatabaseHelper: DatabaseHelper = object : DatabaseHelper {
        override fun eventDatabase(): Flow<EventDatabase> = flowOf(mockEventDatabase)
    }
    private val mockScheduleItemDao = MockScheduleItemDao(elements)

    private val classUnderTest = DatabaseScheduleItemRepository(mockDatabaseHelper, dispatcher)

    init {
        every { mockEventDatabase.scheduleItemDao() } returns mockScheduleItemDao
    }

    override fun afterEach() {}

    @Test
    @DisplayName("Given items are found in database, When calling getAllScheduleItems, Then these items are returned")
    fun getAllScheduleItemsShouldSucceed() {
        runTest {
            val result = classUnderTest.getScheduleItems().first()
            assertThat(result).hasSize(2)
            assertThat(result[0].itemId).isEqualTo(elements[0].id)
            assertThat(result[1].itemId).isEqualTo(elements[1].id)
        }
    }

    @Test
    @DisplayName("Given items are found in database, When calling getListData without a predicate, Then these items are returned")
    fun getListDataWithoutPredicateShouldSucceed() {
        runTest {
            val result = classUnderTest.getListData().first()
            assertThat(result).hasSize(2)
            assertThat(result[0].itemId).isEqualTo(elements[0].id)
            assertThat(result[1].itemId).isEqualTo(elements[1].id)
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getScheduleItemById, Then this item is returned")
    fun getScheduleItemByIdShouldSucceed() {
        runTest {
            val result = classUnderTest.getScheduleItemById(elements[0].id).firstOrNull()
            assertThat(result?.itemId).isEqualTo(elements[0].id)
        }
    }

    @Test
    @DisplayName("Given an item is not found in database, When calling getScheduleItemById, Then null is returned")
    fun getScheduleItemByIdWhenNotExistingShouldSucceed() {
        runTest {
            val result = classUnderTest.getScheduleItemById(99).firstOrNull()
            assertThat(result).isNull()
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getScheduleItemsForActivity, Then this item is returned")
    fun getScheduleItemForActivityShouldSucceed() {
        runTest {
            val result = classUnderTest.getScheduleItemsForActivity(elements[1].activityId).first()
            assertThat(result).hasSize(1)
            assertThat(result.first().itemId).isEqualTo(elements[1].activityId)
        }
    }

    @Test
    @DisplayName("Given an item is not found in database, When calling getScheduleItemsForActivity, Then empty list is returned")
    fun getScheduleItemForActivityNotExistingShouldSucceed() {
        runTest {
            val result = classUnderTest.getScheduleItemsForActivity(99).first()
            assertThat(result).isEmpty()
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getScheduleItemsForTags, Then this item is returned")
    fun getScheduleItemForTagsShouldSucceed() {
        runTest {
            val query = "tags LIKE '%\"tag1\"%'"
            val items = classUnderTest.getScheduleItemsForTags(query).first()

            assertThat(mockScheduleItemDao.sqlQuery).isEqualTo("SELECT * FROM ScheduleItemEntity WHERE $query")
            assertThat(items).hasSize(2)
            assertThat(items[0].itemId).isEqualTo(elements[0].id)
            assertThat(items[1].itemId).isEqualTo(elements[1].id)
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getListData with a predicate, Then these items are returned")
    fun getListDataWithPredicateShouldSucceed() {
        runTest {
            val predicate = FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag1"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag2")
            )
            val items = classUnderTest.getListData(predicate).first()

            assertThat(mockScheduleItemDao.sqlQuery).isEqualTo(
                "SELECT * FROM ScheduleItemEntity WHERE ${
                    predicate.query()?.toSQL()
                }"
            )
            assertThat(items).hasSize(2)
            assertThat(items[0].itemId).isEqualTo(elements[0].id)
            assertThat(items[1].itemId).isEqualTo(elements[1].id)
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getScheduleItemsForTags, Without query, Then this item is returned")
    fun getScheduleItemForTagsWithEmptyQueryShouldSucceed() {
        runTest {
            val items = classUnderTest.getScheduleItemsForTags(null).first()

            assertThat(mockScheduleItemDao.sqlQuery).isEqualTo("SELECT * FROM ScheduleItemEntity ")
            assertThat(items).hasSize(2)
            assertThat(items[0].itemId).isEqualTo(elements[0].id)
            assertThat(items[1].itemId).isEqualTo(elements[1].id)
        }
    }

    @Test
    @DisplayName("Given an exception is thrown from database, When calling getScheduleItemsForTags, Then this exception should be thrown")
    fun getScheduleItemForTagsShouldFail() {
        every {
            mockEventDatabase.scheduleItemDao().getScheduleItemsForTags(any())
        } throws RuntimeException()

        runTest {
            assertThrows<RuntimeException> {
                classUnderTest.getScheduleItemsForTags("QUERY").first()
            }
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getScheduleItemsForPerformer, Then this item is returned")
    fun getScheduleItemForPerformerShouldSucceed() {
        runTest {
            val items = classUnderTest.getScheduleItemsForPerformer("p2").first()

            assertThat(mockScheduleItemDao.performerIdSql).isEqualTo("%\"p2\"%")
            assertThat(items).hasSize(2)
            assertThat(items[0].itemId).isEqualTo(elements[0].id)
            assertThat(items[1].itemId).isEqualTo(elements[1].id)
        }
    }

    @Test
    @DisplayName("Given an exception is thrown from database, When calling getScheduleItemsForPerformer, Then this exception should be thrown")
    fun getScheduleItemForPerformerShouldFail() {
        every {
            mockEventDatabase.scheduleItemDao().getScheduleItemsForPerformer(any())
        } throws RuntimeException()

        runTest {
            assertThrows<RuntimeException> {
                classUnderTest.getScheduleItemsForPerformer("ID").first()
            }
        }
    }
}

private class MockScheduleItemDao(
    var scheduleItems: List<ScheduleItemEntity> = emptyList(),
) : ScheduleItemDao {
    override fun getAllScheduleItems(): List<ScheduleItemEntity> = scheduleItems

    override fun getScheduleItemsForActivity(activityId: Long): List<ScheduleItemEntity> =
        scheduleItems.filter { it.activityId == activityId }

    override fun getScheduleItemById(scheduleItemId: Long): ScheduleItemEntity? =
        scheduleItems.find { it.id == scheduleItemId }

    var sqlQuery: String? = null
        private set

    override fun getScheduleItemsForTags(query: SupportSQLiteQuery): List<ScheduleItemEntity> {
        sqlQuery = query.sql
        return scheduleItems
    }

    var performerIdSql: String? = null
        private set

    override fun getScheduleItemsForPerformer(performerId: String): List<ScheduleItemEntity> {
        performerIdSql = performerId
        return scheduleItems
    }

    var insertedItems: List<ScheduleItemEntity>? = null
        private set

    override fun insertAll(scheduleItems: List<ScheduleItemEntity>) {
        insertedItems = scheduleItems
    }

}
