package com.greencopper.event.dao

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.event.activity.data.database.ActivityDao
import com.greencopper.event.activity.data.database.ContentActivityEntity
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ActivityDaoTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var dao: ActivityDao

    @BeforeEach
    private fun beforeEach() {
        Toolkit.setupTest()
        val db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java).build()
        dao = db.activityDao()
    }

    @Test
    fun getAll_withEmptyDb_returnsEmptyList() {
        runTest {
            val result = dao.getAllActivities()
            assertThat(result.size).isEqualTo(0)
        }
    }

    @Test
    fun getAll_withInserts_returnsList() {
        val insertList = listOf(
            ContentActivityEntity(name = "", photos = listOf()),
            ContentActivityEntity(2, "", "", "", listOf()),
            ContentActivityEntity(3, "", "", "", listOf())
        )
        dao.insertAll(insertList)

        val result = dao.getAllActivities()
        assertThat(result).hasSize(insertList.size)
    }

    @Test
    fun getById_withEmptyDb_returnsNull() {
        runTest {
            val result = dao.getActivityById(3L)
            assertThat(result).isNull()
        }
    }

    @Test
    fun getById_withInserts_returnsCorrectEntity() {
        val firstEntity = ContentActivityEntity(1, "first", "", "", listOf())
        val secondEntity = ContentActivityEntity(2, "second", "", "", listOf())
        val thirdEntity = ContentActivityEntity(3, "third", "", "", listOf())

        dao.insertAll(listOf(firstEntity, secondEntity, thirdEntity))

        val firstResult = dao.getActivityById(firstEntity.id)
        assertThat(firstResult?.name).isEqualTo(firstEntity.name)

        val secondResult = dao.getActivityById(secondEntity.id)
        assertThat(secondResult?.name).isEqualTo(secondEntity.name)
    }
}
