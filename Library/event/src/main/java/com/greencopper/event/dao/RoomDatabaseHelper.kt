package com.greencopper.event.dao

import android.content.Context
import androidx.room.Room
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.d
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

internal class RoomDatabaseHelper(
    private val context: Context,
    eventDatabaseScope: CoroutineScope
) : DatabaseHelper {
    private val eventDatabaseStateFlow: MutableStateFlow<EventDatabase?> =
        MutableStateFlow(null)

    init {
        eventDatabaseScope.launch {
            EventDatabase.databaseFile.collectLatest {
                eventDatabaseStateFlow.value = buildDatabase(it)
            }
        }
    }

    private fun buildDatabase(file: File?): EventDatabase {
        App.log.d("Creating Event database from file $file")
        val databaseBuilder = Room.databaseBuilder(
            context,
            EventDatabase::class.java,
            EventDatabase.DATABASE_NAME
        )
        file?.let {
            databaseBuilder.createFromFile(file)
        }
        return databaseBuilder.build()
    }

    override fun eventDatabase(): Flow<EventDatabase> =
        eventDatabaseStateFlow.filterNotNull()
}
