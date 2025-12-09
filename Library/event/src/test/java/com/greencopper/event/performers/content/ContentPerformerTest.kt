package com.greencopper.event.performers.content

import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Test

internal class ContentPerformerTest {
    init {
        Toolkit.setupTest()
    }

    @Test
    fun testSerializable() {
        val data = ContentPerformer(
            id = "1",
            name = "name",
            subtitle = "subtitle",
            description = "description",
            order = 1,
            photos = listOf("photo1", "photo2"),
            tags = listOf("tag1", "tag2")
        )

        testSerializable(data)
    }

    @Test
    fun testSerializableWithDefaults() {
        val data = ContentPerformer(
            id = "1",
            name = "name",
        )

        testSerializable(data)
    }
}
