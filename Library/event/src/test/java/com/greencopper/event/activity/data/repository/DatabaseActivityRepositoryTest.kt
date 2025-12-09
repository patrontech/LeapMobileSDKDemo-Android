package com.greencopper.event.activity.data.repository

import com.greencopper.event.activity.data.database.ContentActivityEntity
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.dao.EventDatabase
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

internal class DatabaseActivityRepositoryTest: CoroutineTest() {

    private val mockEventDatabase: EventDatabase = mockk()
    private val mockDatabaseHelper: DatabaseHelper = object : DatabaseHelper {
        override fun eventDatabase(): Flow<EventDatabase> = flowOf(mockEventDatabase)
    }
    private val classUnderTest = DatabaseActivityRepository(mockDatabaseHelper, dispatcher)

    private val elementEntity = ContentActivityEntity(
        id = 1,
        name = "",
        subtitle = "",
        description = "",
        photos = emptyList(),
        tags = listOf("tag1", "tag2")
    )

    override fun afterEach() {}

    @Test
    @DisplayName("Given an item is found in database, When calling getActivities, Then this item is returned")
    fun getActivitiesShouldSucceed() {
        every { mockEventDatabase.activityDao().getAllActivities() } returns
                listOf(elementEntity)

        runTest {
            val result = classUnderTest.getActivities().first()
            assertThat(result.first().itemId).isEqualTo(elementEntity.id)
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getListData without a predicate, Then this item is returned")
    fun getListDataWithoutPredicateShouldSucceed() {
        runTest {
            every {
                mockEventDatabase.activityDao().getActivitiesForTags(any())
            } returns listOf(elementEntity)
            val result = classUnderTest.getListData().first()
            assertThat(result).hasSize(1)
            assertThat(result[0].itemId).isEqualTo(elementEntity.id)
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getListData with a predicate, Then this item is returned")
    fun getListDataWithPredicateShouldSucceed() {
        val predicate = FilteringPredicate.Logic(
            FilteringPredicate.Tag("tag1"),
            FilteringPredicate.Operator.OR,
            FilteringPredicate.Tag("tag2")
        )
        every {
            mockEventDatabase.activityDao().getActivitiesForTags(any())
        } returns listOf(elementEntity)

        runTest {
            val result = classUnderTest.getListData(predicate).first()
            assertThat(result.first().itemId).isEqualTo(elementEntity.id)
        }
    }

    @Test
    @DisplayName("Given an exception is thrown from database, When calling getAllScheduleItems, Then this exception should be thrown")
    fun getActivitiesShouldFail() {
        every {
            mockEventDatabase.activityDao().getAllActivities()
        } throws IllegalStateException()

        runTest {
            assertThrows<IllegalStateException> {
                classUnderTest.getActivities().first()
            }
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getActivityById, Then this item is returned")
    fun getActivityByIdShouldSucceed() {
        every {
            mockEventDatabase.activityDao().getActivityById(elementEntity.id)
        } returns elementEntity

        runTest {
            val result = classUnderTest.getActivityById(elementEntity.id).firstOrNull()
            assertThat(result?.itemId).isEqualTo(elementEntity.id)
        }
    }

    @Test
    @DisplayName("Given an exception is thrown from database, When calling getActivityById, Then this exception should be thrown")
    fun getActivityByIdShouldFail() {
        every {
            mockEventDatabase.activityDao().getActivityById(elementEntity.id)
        } throws IllegalArgumentException()

        runTest {
            assertThrows<IllegalArgumentException> {
                classUnderTest.getActivityById(elementEntity.id).firstOrNull()
            }
        }
    }

    @Test
    @DisplayName("Given an item is found in database, When calling getActivityByTag, Then this item is returned")
    fun getActivityByTagShouldSucceed() {
        every {
            mockEventDatabase.activityDao().getActivitiesForTags(any())
        } returns listOf(elementEntity)

        runTest {
            val result = classUnderTest.getActivitiesForTags("QUERY").first()
            assertThat(result.first().itemId).isEqualTo(elementEntity.id)
        }
    }

    @Test
    @DisplayName("Given an exception is thrown from database, When calling getActivityByTag, Then this exception should be thrown")
    fun getActivityByTagShouldFail() {
        every {
            mockEventDatabase.activityDao().getActivitiesForTags(any())
        } throws IllegalArgumentException()

        runTest {
            assertThrows<IllegalArgumentException> {
                classUnderTest.getActivitiesForTags("QUERY").first()
            }
        }
    }
}
