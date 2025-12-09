package com.greencopper.event.timeSlot.data.repository

import com.greencopper.event.timeSlot.TimeSlot
import kotlinx.coroutines.flow.Flow

public interface TimeSlotRepository {
    public suspend fun getTimeSlots(): Flow<List<TimeSlot>>
    public suspend fun getTimeSlotById(timeSlotId: Long): Flow<TimeSlot?>
    public suspend fun getTimeSlotForScheduleItem(scheduleItemId: Long): Flow<TimeSlot?>
}
