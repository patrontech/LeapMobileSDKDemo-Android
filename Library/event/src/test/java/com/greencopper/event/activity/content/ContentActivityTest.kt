package com.greencopper.event.activity.content

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

internal class ContentActivityTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()
    private val data = ContentActivity(
        id = 1,
        name = "name",
        subtitle = "subtitle",
        description = "description",
        photos = listOf("photo1", "photo2"),
        tags = listOf("tag1", "tag2")
    )

    @Test
    fun serializeAndDeserialize() {
        assertDoesNotThrow {

            val dataString = json.encodeToString(data)
            val copyData = json.decodeFromString<ContentActivity>(dataString)

            Assertions.assertThat(copyData.id).isEqualTo(data.id)
        }
    }
}