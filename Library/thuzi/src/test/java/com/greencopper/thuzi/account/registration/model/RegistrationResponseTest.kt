package com.greencopper.thuzi.account.registration.model

import com.greencopper.testmocks.testKiboSerializable
import kotlinx.serialization.json.JsonNull
import org.junit.jupiter.api.Test

internal class RegistrationResponseTest {
    @Test
    fun testRegistrationResponse() = testKiboSerializable(
        RegistrationResponse(
            type = RegistrationResponse.ACTIVATION_COMPLETE,
            data = JsonNull
        )
    )
}
