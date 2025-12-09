package com.greencopper.event.mock.database

import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.dao.EventDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class MockDatabaseHelper: DatabaseHelper {

    private val timedScheduleItemDao = MockTimedScheduleItemDao()

    override fun eventDatabase(): Flow<EventDatabase> = flowOf(
        mockk<EventDatabase>().apply {
            every { timedScheduleItemDao() } returns timedScheduleItemDao
        }
    )
}