package com.greencopper.event.stage.data.repository

import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.dao.EventDatabase
import com.greencopper.event.stage.data.StageDao
import com.greencopper.event.stage.data.StageEntity
import com.greencopper.testmocks.CoroutineTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DatabaseStageRepositoryTest : CoroutineTest() {

    private var mockedStageDao = mockk<StageDao>()
    private var mockedEventDatabase = mockk<EventDatabase>()
    private var mockedDatabaseHelper = mockk<DatabaseHelper>()
    private val stageRepository = DatabaseStageRepository(mockedDatabaseHelper, dispatcher)

    init {
        every { mockedEventDatabase.stageDao() } returns mockedStageDao
        every { mockedDatabaseHelper.eventDatabase() } returns flow { emit(mockedEventDatabase) }
    }

    override fun afterEach() {}

    @Test
    fun whenGettingStages_withoutAnyInDatabase_returnEmpty() {
        every { mockedStageDao.getAllStages() } returns emptyList()

        runTest {
            val stages = stageRepository.getStages().first()
            assertThat(stages).isEmpty()
        }
    }

    @Test
    fun whenGettingStages_withSomeInDatabase_returnSome() {
        val stage1 = StageEntity(1, "SomeStage", null, emptyList())
        val stage2 = StageEntity(2, "OtherStage", "Subtitle", emptyList())
        val databaseStages = listOf(stage1, stage2)
        every { mockedStageDao.getAllStages() } returns databaseStages

        runTest {
            val stages = stageRepository.getStages().first()
            assertThat(stages.size).isEqualTo(2)
            assertThat(stages[0].id).isEqualTo(stage1.id)
            assertThat(stages[1].id).isEqualTo(stage2.id)
        }
    }

    @Test
    fun whenGettingStageByID_withOneInDatabase_returnCorrectOne() {
        val stage1 = StageEntity(1, "SomeStage", null, emptyList())
        every { mockedStageDao.getStageForId(1) } returns stage1

        runTest {
            val stage = stageRepository.getStageForId(1).firstOrNull()
            assertThat(stage).isNotNull
            assertThat(stage!!.name).isEqualTo(stage1.name)
        }
    }

    @Test
    fun whenGettingStageByID_withoutOneInDatabase_returnNull() {
        every { mockedStageDao.getStageForId(1) } returns null

        runTest {
            val wrongStage = stageRepository.getStageForId(1).firstOrNull()
            assertThat(wrongStage).isNull()
        }
    }

    @Test
    fun whenGettingStagesByTags_withSomeInDatabase_returnSome() {
        val stage1 = StageEntity(1, "SomeStage", null, emptyList(), emptyList())
        every { mockedStageDao.getStagesForTags(any()) } returns listOf(stage1)

        runTest {
            val stages = stageRepository.getStagesForTags("query").first()
            assertThat(stages[0].id).isEqualTo(stage1.id)
        }
    }
}
