package com.greencopper.event

import java.io.File

internal interface EventDataProcessor {
    suspend fun process(unarchivedDirectory: File, contentDirectory: File)
    suspend fun apply(contentDirectory: File)
}
