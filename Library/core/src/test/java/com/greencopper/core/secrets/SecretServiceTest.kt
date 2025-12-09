package com.greencopper.core.secrets

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SecretServiceTest {
    @Test
    fun whenReadingSecrets_shouldReadProperSecret() {
        val otaZip = "abc123"
        val notificationRegistrationApi = "xyz123"
        val remoteStateApi = "89674523"
        val secrets: Map<String, String> = mapOf(
            "otaZip" to otaZip,
            "notificationRegistrationApi" to notificationRegistrationApi,
            "remoteStateApi" to remoteStateApi
        )
        val service = SecretService(secrets)

        assertThat(service.otaZip).isEqualTo(otaZip)
        assertThat(service.notificationRegistrationApi).isEqualTo(notificationRegistrationApi)
        assertThat(service.remoteStateApi).isEqualTo(remoteStateApi)
    }

    @Test
    fun whenMissingOtaZip_shouldThrow() {
        val secrets: Map<String, String> = mapOf(
            "test1" to "test2",
        )
        val service = SecretService(secrets)

        assertThrows<SecretMissingException> {
            service.otaZip
        }
    }

    @Test
    fun whenMissingNotificationRegistrationApi_shouldThrow() {
        val secrets: Map<String, String> = mapOf(
            "test1" to "test2",
        )
        val service = SecretService(secrets)

        assertThrows<SecretMissingException> {
            service.notificationRegistrationApi
        }
    }

    @Test
    fun whenMissingRemoteStateApi_shouldThrow() {
        val secrets: Map<String, String> = mapOf(
            "test1" to "test2",
        )
        val service = SecretService(secrets)

        assertThrows<SecretMissingException> {
            service.remoteStateApi
        }
    }
}