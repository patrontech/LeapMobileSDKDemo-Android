package com.greencopper.event.dao

import kotlinx.coroutines.flow.Flow

internal interface DatabaseHelper {
    fun eventDatabase(): Flow<EventDatabase>
}