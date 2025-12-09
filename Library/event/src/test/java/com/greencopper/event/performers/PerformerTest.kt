package com.greencopper.event.performers

import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Test

internal class PerformerTest {
    init {
        Toolkit.setupTest()
    }

    @Test
    fun testSerializable() {
        val data = Performer(
            itemId = "1",
            name = "name",
            subtitle = "subtitle",
            description = "description",
            order = 1,
            photos = listOf("photo1", "photo2"),
            tags = listOf("tag1", "tag2")
        )

        testKiboSerializable(data)
    }

    @Test
    fun testSerializableWithDefaults() {
        val data = Performer(
            itemId = "1",
            name = "name",
        )

        testKiboSerializable(data)
    }
}
