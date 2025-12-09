package com.greencopper.event.mock

import com.greencopper.event.EventDataProcessor
import java.io.File

internal class MockEventDataProcessor(
    val process: () -> Unit = { },
    val apply: () -> Unit = { }
) : EventDataProcessor {

    var processCount = 0
        private set

    var applyCount = 0
        private set

    override suspend fun process(unarchivedDirectory: File, contentDirectory: File) =
        process().also { processCount += 1 }

    override suspend fun apply(contentDirectory: File) = apply().also { applyCount += 1 }

    fun reset() {
        processCount = 0
        applyCount = 0
    }
}