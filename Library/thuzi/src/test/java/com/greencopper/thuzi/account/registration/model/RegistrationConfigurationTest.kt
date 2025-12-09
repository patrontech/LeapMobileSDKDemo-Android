package com.greencopper.thuzi.account.registration.model

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RegistrationConfigurationTest {
    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
    }

    @Test
    fun testRegistrationConfiguration() = testKiboSerializable(RegistrationConfiguration(
        apiUrl = "apiUrl",
        activationUrl = "activationUrl",
        deviceLinkingUrl = "deviceLinkingUrl",
        userStateUpdateUrl = "userStateUpdateUrl",
        brandId = "brandId",
        eventId = "eventId",
        project = "project",
        analytics = ScreenNameAnalytics("screenName"),
        accountDeletionApiUrl = "accountDeletionApiUrl",
    ))

    @Test
    fun testRegistrationConfigurationFromLegacyJSON() {
        // When *Session values aren't present, it
        // shouldn't barf. It should fall back to the
        // legacy values.
        val json = """
            {
                "apiUrl": "apiUrl",
                "accountDeletionApiUrl": "accountDeletionApiUrl",
                "activationUrl": "activationUrl",
                "deviceLinkingUrl": "deviceLinkingUrl",
                "userStateUpdateUrl": "userStateUpdateUrl",
                "brandId": "brandId",
                "eventId": "eventId",
                "project": "project",
                "analytics": {"screenName": "screenName"}
            }
        """.trimIndent()

        val configuration = KiboSerializable.decodeFromString<RegistrationConfiguration>(json)
        assertThat(configuration.activationUrl).isEqualTo("activationUrl")
        assertThat(configuration.deviceLinkingUrl).isEqualTo("deviceLinkingUrl")
    }

    @Test
    fun testRegistrationConfigurationFromSessionJSON() {
        // When both *Session and non-*Session values are
        // present, it should use only the *Session values.
        val json = """
            {
                "apiUrl": "apiUrl",
                "accountDeletionApiUrl": "accountDeletionApiUrl",
                "activationUrl": "activationUrl",
                "activationSessionUrl": "activationSessionUrl",
                "deviceLinkingUrl": "deviceLinkingUrl",
                "deviceLinkingUrl": "deviceLinkingSessionUrl",
                "userStateUpdateUrl": "userStateUpdateUrl",
                "brandId": "brandId",
                "eventId": "eventId",
                "project": "project",
                "analytics": {"screenName": "screenName"}
            }
        """.trimIndent()

        val configuration = KiboSerializable.decodeFromString<RegistrationConfiguration>(json)
        assertThat(configuration.activationUrl).isEqualTo("activationSessionUrl")
        assertThat(configuration.deviceLinkingUrl).isEqualTo("deviceLinkingSessionUrl")
    }
}
