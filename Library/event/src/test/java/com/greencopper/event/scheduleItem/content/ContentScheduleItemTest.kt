package com.greencopper.event.scheduleItem.content

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class ContentScheduleItemTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()

    @Test
    fun serializeAndDeserialize() {
        assertDoesNotThrow {
            val data = ContentScheduleItem(
                id = 1,
                activityId = 2,
                stageId = 3,
                name = "name",
                subtitle = "subtitle",
                description = "description",
                photos = listOf("photo1", "photo2"),
                tags = listOf("tag1", "tag2"),
                performerIds = listOf("perf1", "perf2")
            )

            val dataString = json.encodeToString(data)
            val copyData = json.decodeFromString<ContentScheduleItem>(dataString)

            assertThat(copyData.id).isEqualTo(data.id)
        }
    }

    @Test
    fun serializeAndDeserializeWithDefaults() {
        assertDoesNotThrow {
            val data = ContentScheduleItem(
                id = 1,
                activityId = 2,
                name = "name",
                subtitle = "subtitle",
                description = "description",
                photos = listOf("photo1", "photo2"),
            )

            val dataString = json.encodeToString(data)
            val copyData = json.decodeFromString<ContentScheduleItem>(dataString)

            assertThat(copyData.id).isEqualTo(data.id)
        }
    }

}
