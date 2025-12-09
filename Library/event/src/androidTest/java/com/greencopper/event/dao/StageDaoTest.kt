package com.greencopper.event.dao

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.event.stage.data.StageDao
import com.greencopper.event.stage.data.StageEntity
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StageDaoTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var dao: StageDao

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        val db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java).build()
        dao = db.stageDao()
    }

    @Test
    fun getAll_withEmptyDb_returnsEmptyList() {
        runTest {
            val result = dao.getAllStages()
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun getAll_withInserts_returnsList() {
        val insertList = listOf(
            StageEntity(name = "", photos = listOf()),
            StageEntity(2, "", "", listOf())
        )

        dao.insertAll(insertList)
        val result = dao.getAllStages()
        assertThat(result).hasSameSizeAs(insertList)
    }

    @Test
    fun getById_withEmptyDb_returnsNull() {
        runTest {
            val result = dao.getStageForId(1)
            assertThat(result).isNull()
        }
    }

    @Test
    fun getById_withInserts_returnsCorrectEntity() {
        val firstEntity = StageEntity(1, "", "", listOf())
        val secondEntity = StageEntity(2, "", "", listOf())
        val thirdEntity = StageEntity(3, "", "", listOf())

        dao.insertAll(listOf(firstEntity, secondEntity, thirdEntity))

        val firstResult = dao.getStageForId(firstEntity.id)
        assertThat(firstResult?.id).isEqualTo(firstEntity.id)

        val thirdResult = dao.getStageForId(thirdEntity.id)
        assertThat(thirdResult?.id).isEqualTo(thirdEntity.id)
    }
}
