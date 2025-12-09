package com.greencopper.event.timeSlot.content

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class ContentTimeSlotTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()
    private val data = ContentTimeSlot(
        id = 1,
        scheduleItemId = 2,
        dayOfEventText = "",
        startDateText = null,
        endDateText = null
    )

    @Test
    fun serializeAndDeserialize() {
        assertDoesNotThrow {

            val dataString = json.encodeToString(data)
            val copyData = json.decodeFromString<ContentTimeSlot>(dataString)

            Assertions.assertThat(copyData.id).isEqualTo(data.id)
        }
    }
}