package com.greencopper.event.dao

import androidx.room.*
import com.greencopper.event.activity.data.database.ActivityDao
import com.greencopper.event.activity.data.database.ContentActivityEntity
import com.greencopper.event.data.database.TimedScheduleItemDao
import com.greencopper.event.performers.data.database.PerformerDao
import com.greencopper.event.performers.data.database.PerformerEntity
import com.greencopper.event.scheduleItem.data.database.ScheduleItemDao
import com.greencopper.event.scheduleItem.data.database.ScheduleItemEntity
import com.greencopper.event.stage.data.StageDao
import com.greencopper.event.stage.data.StageEntity
import com.greencopper.event.timeSlot.data.database.TimeSlotDao
import com.greencopper.event.timeSlot.data.database.TimeSlotEntity
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File

@Database(
    entities = [ContentActivityEntity::class, ScheduleItemEntity::class, TimeSlotEntity::class, StageEntity::class, PerformerEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
internal abstract class EventDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun scheduleItemDao(): ScheduleItemDao
    abstract fun timeSlotDao(): TimeSlotDao
    abstract fun stageDao(): StageDao
    abstract fun timedScheduleItemDao(): TimedScheduleItemDao
    abstract fun performerDao(): PerformerDao

    companion object {
        val databaseFile: MutableStateFlow<File?> = MutableStateFlow(null)
        const val DATABASE_NAME = "event-database"
    }
}

internal class Converters {

    private val json: Json
        get() = App.resolve()

    @TypeConverter
    fun restoreList(listOfString: String): List<String> =
        json.decodeFromString(ListSerializer(String.serializer()), listOfString)

    @TypeConverter
    fun saveList(listOfString: List<String>): String =
        json.encodeToString(ListSerializer(String.serializer()), listOfString)
}
