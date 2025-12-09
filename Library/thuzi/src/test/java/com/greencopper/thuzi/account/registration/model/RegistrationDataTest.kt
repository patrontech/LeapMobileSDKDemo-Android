package com.greencopper.thuzi.account.registration.model

import com.greencopper.testmocks.testKiboSerializable
import org.junit.jupiter.api.Test

internal class RegistrationDataTest {
    @Test
    fun testRegistration() = testKiboSerializable(
        RegistrationData(
            qrCode = "qrCode",
            attendeeId = "attendeeId",
            authToken = "jwt",
            authTokenExpiresOn = "tokenExpiry",
            attendee = RegistrationData.Attendee(),
        )
    )

    @Test
    fun tesAttendee_withName() = testKiboSerializable(
        RegistrationData.Attendee("firstName")
    )

    @Test
    fun tesAttendee_withoutName() = testKiboSerializable(
        RegistrationData.Attendee(null)
    )
}
