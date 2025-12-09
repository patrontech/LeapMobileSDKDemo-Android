package com.greencopper.thuzi.account.registration.model

import com.greencopper.testmocks.testKiboSerializable
import org.junit.jupiter.api.Test

internal class CompletionDataTest {
    @Test
    fun testCompletionData() = testKiboSerializable(
        CompletionData(true)
    )
}