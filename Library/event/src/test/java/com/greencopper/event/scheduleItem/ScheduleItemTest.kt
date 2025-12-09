package com.greencopper.event.scheduleItem

import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Test

internal class ScheduleItemTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun testSerializable() {
        val item = ScheduleItem(
            itemId = 10,
            activityId = 20,
            stageId = 30,
            name = "Test Name",
            subtitle = "Test Subtitle",
            description = "Test Description",
            photos = listOf("testPhoto.png"),
            tags = listOf("tag1", "tag2"),
            performerIds = listOf("perf1", "perf2")
        )

        testKiboSerializable(item)
    }

    @Test
    fun testSerializableWithDefaults() {
        val item = ScheduleItem(
            itemId = 10,
            activityId = 20,
            name = "Test Name",
            photos = listOf("testPhoto.png"),
        )

        testKiboSerializable(item)
    }
}
