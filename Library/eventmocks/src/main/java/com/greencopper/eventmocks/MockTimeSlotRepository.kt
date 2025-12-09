package com.greencopper.eventmocks

import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.event.timeSlot.data.repository.TimeSlotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockTimeSlotRepository(public var returnData: List<TimeSlot> = emptyList()) : TimeSlotRepository {

    override suspend fun getTimeSlots(): Flow<List<TimeSlot>> =
        flowOf(returnData)

    override suspend fun getTimeSlotById(timeSlotId: Long): Flow<TimeSlot?> =
        flowOf(returnData.find { it.id == timeSlotId })

    override suspend fun getTimeSlotForScheduleItem(scheduleItemId: Long): Flow<TimeSlot?> =
        flowOf(returnData.find { it.scheduleItemId == scheduleItemId })
}
