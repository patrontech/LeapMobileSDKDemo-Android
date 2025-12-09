package com.greencopper.event.timeSlot.data.repository

import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.event.timeSlot.toDataModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

internal class DatabaseTimeSlotRepository(
    private val databaseHelper: DatabaseHelper,
    private val backgroundContext: CoroutineContext,
) : TimeSlotRepository {

    override suspend fun getTimeSlots(): Flow<List<TimeSlot>> =
        databaseHelper.eventDatabase().map {
            it.timeSlotDao()
                .getAllTimeSlots()
                .map { timeSlotEntity -> timeSlotEntity.toDataModel() }
        }.flowOn(backgroundContext)

    override suspend fun getTimeSlotById(timeSlotId: Long): Flow<TimeSlot?> =
        databaseHelper.eventDatabase().map {
            it.timeSlotDao()
                .getTimeSlotById(timeSlotId)
                ?.toDataModel()
        }.flowOn(backgroundContext)

    override suspend fun getTimeSlotForScheduleItem(scheduleItemId: Long): Flow<TimeSlot?> =
        databaseHelper.eventDatabase().map {
            it.timeSlotDao()
                .getTimeSlotsByScheduleItem(scheduleItemId)
                .firstOrNull()?.toDataModel()
        }.flowOn(backgroundContext)
}