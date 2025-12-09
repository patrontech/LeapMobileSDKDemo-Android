package com.greencopper.event.timeSlot.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface TimeSlotDao {

    @Query("SELECT * FROM TimeSlotEntity")
    fun getAllTimeSlots(): List<TimeSlotEntity>

    @Insert
    fun insertAll(timeSlots: List<TimeSlotEntity>)

    @Query("SELECT * FROM TimeSlotEntity WHERE id LIKE :timeSlotId")
    fun getTimeSlotById(timeSlotId: Long): TimeSlotEntity?

    @Query("SELECT * FROM TimeSlotEntity WHERE scheduleItemId LIKE :scheduleItemId")
    fun getTimeSlotsByScheduleItem(scheduleItemId: Long): List<TimeSlotEntity>

    @Query("SELECT * FROM TimeSlotEntity WHERE scheduleItemId IN (:scheduleItemIds)")
    fun getTimeSlotsByScheduleItems(scheduleItemIds: List<Long>): List<TimeSlotEntity>
}