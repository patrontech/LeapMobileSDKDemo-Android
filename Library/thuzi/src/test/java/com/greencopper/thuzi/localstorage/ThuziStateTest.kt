package com.greencopper.thuzi.localstorage

import com.greencopper.testmocks.testKiboSerializable
import org.junit.jupiter.api.Test

internal class ThuziStateTest {

    @Test
    fun testSerializable() {
        val thuziState = ThuziState(
            answers = mapOf("1" to "test1", "2" to "test2"),
            attendee = Attendee(postalCode = "123456")
        )

        testKiboSerializable(thuziState)
    }
}
