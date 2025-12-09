package com.greencopper.event

import android.content.Context
import androidx.room.Room
import com.greencopper.event.activity.content.ContentActivity
import com.greencopper.event.activity.content.toEntityModel
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.dao.EventDatabase
import com.greencopper.event.performers.content.ContentPerformer
import com.greencopper.event.performers.content.toEntityModel
import com.greencopper.event.scheduleItem.content.ContentScheduleItem
import com.greencopper.event.scheduleItem.content.toEntityModel
import com.greencopper.event.stage.content.ContentStage
import com.greencopper.event.stage.content.toEntityModel
import com.greencopper.event.timeSlot.content.ContentTimeSlot
import com.greencopper.event.timeSlot.content.toEntityModel
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.i
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

internal class RoomEventDataProcessor(
    private val jsonParser: Json,
    private val context: Context,
    private val databaseHelper: DatabaseHelper,
    private val eventDatabaseScope: CoroutineScope,
) : EventDataProcessor {

    private lateinit var updateDatabase: EventDatabase

    override suspend fun process(unarchivedDirectory: File, contentDirectory: File) =
        withContext(eventDatabaseScope.coroutineContext) {
            val databaseName = "updateDB"
            updateDatabase =
                Room.databaseBuilder(context, EventDatabase::class.java, databaseName)
                    .build()
            updateDatabase.clearAllTables()
            var totalItems = 0
            try {
                totalItems += parseActivities(unarchivedDirectory)
                totalItems += parseScheduleItems(unarchivedDirectory)
                totalItems += parseTimeSlots(unarchivedDirectory)
                totalItems += parseStages(unarchivedDirectory)
                totalItems += parsePerformers(unarchivedDirectory)
            } finally {
                updateDatabase.close()
            }
            val databaseFile = context.getDatabasePath(databaseName)
            databaseFile.copyTo(contentDirectory.databasePath(), overwrite = true)
            context.deleteDatabase(databaseName)
            App.log.i("New database successfully processed with $totalItems items.")
        }

    private fun parseActivities(unarchivedDirectory: File): Int {
        val dataFile = File(unarchivedDirectory, "activities.json")
        require(dataFile.exists()) { "Activities data file doesn't exist" }
        val activities =
            jsonParser.decodeFromString(
                ListSerializer(ContentActivity.serializer()),
                dataFile.readText()
            ).filter { it.id != -1L }
                .map { it.toEntityModel() }
        updateDatabase.activityDao().insertAll(activities)
        return activities.size
    }

    private fun parseScheduleItems(unarchivedDirectory: File): Int {
        val dataFile = File(unarchivedDirectory, "scheduleItems.json")
        require(dataFile.exists()) { "Schedule items data file doesn't exist" }
        val scheduleItems =
            jsonParser.decodeFromString(
                ListSerializer(ContentScheduleItem.serializer()),
                dataFile.readText()
            ).filter { it.id != -1L }
                .map { it.toEntityModel() }
        runBlocking {
            updateDatabase.scheduleItemDao().insertAll(scheduleItems)
        }
        return scheduleItems.size
    }

    private fun parseTimeSlots(unarchivedDirectory: File): Int {
        val dataFile = File(unarchivedDirectory, "timeSlots.json")
        require(dataFile.exists()) { "Time slots data file doesn't exist" }
        val timeSlots =
            jsonParser.decodeFromString(
                ListSerializer(ContentTimeSlot.serializer()),
                dataFile.readText()
            ).filter { it.id != -1L }
                .map { it.toEntityModel() }
        runBlocking {
            updateDatabase.timeSlotDao().insertAll(timeSlots)
        }
        return timeSlots.size
    }

    private fun parsePerformers(unarchivedDirectory: File): Int {
        val dataFile = File(unarchivedDirectory, "performers.json")
        require(dataFile.exists()) { "Performers data file doesn't exist" }
        val performers =
            jsonParser.decodeFromString(
                ListSerializer(ContentPerformer.serializer()),
                dataFile.readText()
            ).filter { it.id != "-1" }
                .map { it.toEntityModel() }
        runBlocking {
            updateDatabase.performerDao().insertAll(performers)
        }
        return performers.size
    }

    private fun parseStages(unarchivedDirectory: File): Int {
        val dataFile = File(unarchivedDirectory, "stages.json")
        require(dataFile.exists()) { "Stages data file doesn't exist" }
        val stages =
            jsonParser.decodeFromString(
                ListSerializer(ContentStage.serializer()),
                dataFile.readText()
            ).filter { it.id != -1L }
                .map { it.toEntityModel() }
        updateDatabase.stageDao().insertAll(stages)
        return stages.size
    }

    private fun File.databasePath(): File =
        File(this, EventDatabase.DATABASE_NAME)

    override suspend fun apply(contentDirectory: File) =
        withContext(eventDatabaseScope.coroutineContext) {
            val eventDatabase = databaseHelper.eventDatabase().first()

            // set file to null while database is being closed and deleted, so nothing can try and access it
            EventDatabase.databaseFile.value = null

            eventDatabase.close()
            context.deleteDatabase(EventDatabase.DATABASE_NAME)

            EventDatabase.databaseFile.value = contentDirectory.databasePath()

            App.log.i("New database successfully applied.")
        }
}

