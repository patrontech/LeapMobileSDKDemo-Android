package com.greencopper.event.dao

import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.event.performers.data.database.PerformerDao
import com.greencopper.event.performers.data.database.PerformerEntity
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PerformerDaoTest {
    private val context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var dao: PerformerDao

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        val db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java).build()
        dao = db.performerDao()
    }

    @Test
    fun getAll_withEmptyDb_returnsEmptyList() {
        runTest {
            val result = dao.getAllPerformers()
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun getAll_withInserts_returnsList() {
        val insertList = listOf(
            PerformerEntity("1", "SomePerformer"),
            PerformerEntity("2", "SomePerformer")
        )

        dao.insertAll(insertList)
        val result = dao.getAllPerformers()
        assertThat(result).hasSameSizeAs(insertList)
        assertThat(result).isEqualTo(insertList)
    }

    @Test
    fun getById_withEmptyDb_returnsNull() {
        runTest {
            val result = dao.getPerformerById("1")
            assertThat(result).isNull()
        }
    }

    @Test
    fun getById_withInserts_returnsCorrectEntity() {
        val firstEntity =
            PerformerEntity("1", "SomePerformer")
        val secondEntity =
            PerformerEntity("2", "SomePerformer")
        val thirdEntity =
            PerformerEntity("3", "SomePerformer")

        dao.insertAll(listOf(firstEntity, secondEntity, thirdEntity))

        assertThat(dao.getPerformerById(firstEntity.id)?.id).isEqualTo(firstEntity.id)
        assertThat(dao.getPerformerById(thirdEntity.id)?.id).isEqualTo(thirdEntity.id)
    }

    @Test
    fun getByTags_withInserts_returnsCorrectEntity() {
        val firstEntity =
            PerformerEntity("1",
                "SomePerformer",
                tags = listOf("pouet", "performer1"))
        val secondEntity =
            PerformerEntity("2",
                "SomePerformer",
                tags = listOf("performer2", "pouet"))
        val thirdEntity =
            PerformerEntity("3", "SomePerformer", tags = listOf("performer3"))
        dao.insertAll(listOf(firstEntity, secondEntity, thirdEntity))

        val request1 =
            SimpleSQLiteQuery("SELECT * FROM PerformerEntity WHERE tags LIKE '%\"performer1\"%'")
        val result1 = dao.getPerformersForTags(request1)
        assertThat(result1.size).isEqualTo(1)
        assertThat(result1[0].id).isEqualTo("1")

        val request2 =
            SimpleSQLiteQuery("SELECT * FROM PerformerEntity WHERE tags LIKE '%\"pouet\"%' OR tags LIKE '%\"pouet\"%'")
        val result2 = dao.getPerformersForTags(request2)
        assertThat(result2.size).isEqualTo(2)
        assertThat(result2).contains(firstEntity, secondEntity)

        val request3 =
            SimpleSQLiteQuery("SELECT * FROM PerformerEntity WHERE tags LIKE '%\"test\"%'")
        val result3 = dao.getPerformersForTags(request3)
        assertThat(result3).isEmpty()
    }
}

