package com.greencopper.thuzi.models

import com.greencopper.testmocks.testKiboSerializable
import org.junit.jupiter.api.Test

internal class DispatchedThuziStateTest {

    @Test
    fun testSerializable() {
        val thuziState = DispatchedThuziState(
            registration = Registration(true),
            answers = mapOf("1" to "test1", "2" to "test2"),
            attendee = Attendee(postalCode = "123456")
        )

        testKiboSerializable(thuziState)
    }

    @Test
    fun testSerializable_secondaryConstructor() {
        val thuziState = DispatchedThuziState(
            true,
            mapOf("1" to "test1", "2" to "test2"),
            postalCode = "123456"
        )

        testKiboSerializable(thuziState)
    }

}