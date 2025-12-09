package com.greencopper.thuzi.account.registration

import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.account.registration.model.RegistrationData
import com.greencopper.thuzi.account.registration.model.RegistrationResponse
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PTWebViewInterfaceTest {
    @Test
    fun testJSInterface() {
        Toolkit.setupTest()

        val json: Json = App.resolve()

        val testData = RegistrationResponse(
            type = RegistrationResponse.ATTENDEE_AUTHENTICATED,
            data = json.encodeToJsonElement(
                RegistrationData(
                    qrCode = "qrCode",
                    attendeeId = "attendeeId",
                    authTokenExpiresOn = "jwt",
                    authToken = "tokenExpiry",
                    attendee = RegistrationData.Attendee(),
                )
            )
        )

        val jsInterface = PTWebViewInterface { data ->
            assertThat(data).isEqualTo(testData)
        }

        jsInterface.postMessage(testData.encodeToString())
    }
}
