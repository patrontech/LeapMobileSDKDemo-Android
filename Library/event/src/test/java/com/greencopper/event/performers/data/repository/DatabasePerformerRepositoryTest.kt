package com.greencopper.event.performers.data.repository

import androidx.sqlite.db.SupportSQLiteQuery
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.dao.EventDatabase
import com.greencopper.event.performers.data.database.PerformerDao
import com.greencopper.event.performers.data.database.PerformerEntity
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

internal class DatabasePerformerRepositoryTest : CoroutineTest() {

    private val databasePerformers = listOf(
        PerformerEntity(
            "1",
            "SomePerformer",
            null,
            null,
        ),
        PerformerEntity(
            "2",
            "OtherPerformer",
            "Subtitle",
            "Description",
        )
    )

    private val performerRepo: DatabasePerformerRepository
    private val mockPerformerDao: MockPerformerDao = MockPerformerDao(databasePerformers)
    private val mockEventDatabase: EventDatabase = mockk()
    private val mockDatabaseHelper: DatabaseHelper = object : DatabaseHelper {
        override fun eventDatabase(): Flow<EventDatabase> = flowOf(mockEventDatabase)
    }

    init {
        every { mockEventDatabase.performerDao() } returns mockPerformerDao
        performerRepo = DatabasePerformerRepository(mockDatabaseHelper, dispatcher)
    }

    override fun afterEach() {}

    @Test
    fun whenGettingPerformers_withoutAnyInDatabase_returnEmpty() {
        mockPerformerDao.performers = emptyList()

        runTest {
            val performers = performerRepo.getPerformers().first()
            assertThat(performers).isEmpty()
        }
    }

    @Test
    fun whenGettingPerformers_withSomeInDatabase_returnSome() {
        runTest {
            val performers = performerRepo.getPerformers().first()
            assertThat(performers).hasSize(2)
            assertThat(performers[0].itemId).isEqualTo(databasePerformers[0].id)
            assertThat(performers[1].itemId).isEqualTo(databasePerformers[1].id)
        }
    }

    @Test
    fun whenGettingPerformerById_withOneInDatabase_returnCorrectOne() {
        runTest {
            val performer = performerRepo.getPerformerById("1").firstOrNull()
            assertThat(performer).isNotNull
            assertThat(performer!!.name).isEqualTo(databasePerformers[0].name)
        }
    }

    @Test
    fun whenGettingPerformerById_withoutOneInDatabase_returnNull() {
        runTest {
            val wrongPerformer = performerRepo.getPerformerById("3").firstOrNull()
            assertThat(wrongPerformer).isNull()
        }
    }

    @Test
    fun whenGettingPerformersByTags_withSomeInDatabase_returnSome() {
        runTest {
            val query = "tags LIKE '%\"tag1\"%'"
            val performers = performerRepo.getPerformersForTags(query).first()

            assertThat(mockPerformerDao.sqlQuery).isEqualTo("SELECT * FROM PerformerEntity WHERE $query")
            assertThat(performers).hasSize(2)
            assertThat(performers[0].itemId).isEqualTo(databasePerformers[0].id)
            assertThat(performers[1].itemId).isEqualTo(databasePerformers[1].id)
        }
    }

    @Test
    fun whenGettingPerformersByTags_withSomeInDatabase_withEmptyQuery_returnAll() {
        runTest {
            val performers = performerRepo.getPerformersForTags(null).first()

            assertThat(mockPerformerDao.sqlQuery).isEqualTo("SELECT * FROM PerformerEntity ")
            assertThat(performers).hasSize(2)
            assertThat(performers[0].itemId).isEqualTo(databasePerformers[0].id)
            assertThat(performers[1].itemId).isEqualTo(databasePerformers[1].id)
        }
    }

    @Test
    @DisplayName("Given items are found in database, When calling getListData without a predicate, Then these items are returned")
    fun getListDataWithoutPredicateShouldSucceed() {
        runTest {
            val result = performerRepo.getListData().first()
            assertThat(result).hasSize(2)
            assertThat(result[0].itemId).isEqualTo(databasePerformers[0].id)
            assertThat(result[1].itemId).isEqualTo(databasePerformers[1].id)
        }
    }

    @Test
    @DisplayName("Given items are found in database, When calling getListData with a predicate, Then these items are returned")
    fun getListDataWithPredicateShouldSucceed() {
        runTest {
            val predicate = FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag1"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag2")
            )
            val items = performerRepo.getListData(predicate).first()

            assertThat(mockPerformerDao.sqlQuery).isEqualTo(
                "SELECT * FROM PerformerEntity WHERE ${
                    predicate.query()?.toSQL()
                }"
            )
            assertThat(items).hasSize(2)
            assertThat(items[0].itemId).isEqualTo(databasePerformers[0].id)
            assertThat(items[1].itemId).isEqualTo(databasePerformers[1].id)
        }
    }
}

private class MockPerformerDao(
    var performers: List<PerformerEntity> = emptyList(),
) : PerformerDao {

    var insertedPerformers: List<PerformerEntity>? = null
        private set

    override fun insertAll(performers: List<PerformerEntity>) {
        insertedPerformers = performers
    }

    override fun getAllPerformers(): List<PerformerEntity> = performers

    override fun getPerformerById(performerId: String): PerformerEntity? =
        performers.find { it.id == performerId }

    var sqlQuery: String? = null
        private set

    override fun getPerformersForTags(query: SupportSQLiteQuery): List<PerformerEntity> {
        sqlQuery = query.sql
        return performers
    }

}
