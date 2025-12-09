package com.greencopper.event.mock

import android.content.Context
import androidx.room.Room
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.dao.EventDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class MockDatabaseHelper(context: Context) : DatabaseHelper {

    private val eventDatabaseStateFlow: MutableStateFlow<EventDatabase> =
        MutableStateFlow(Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java).build())

    override fun eventDatabase(): Flow<EventDatabase> =  eventDatabaseStateFlow
}