package com.greencopper.thuzi.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class DeviceSessionTest {
    @Test
    fun testSerializable() {
        val installationId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID()
        val urn = "urn:device-session:$installationId:$sessionId"

        var deviceSession = DeviceSession(installationId, sessionId)
        assertThat(deviceSession.urn).isEqualTo(urn)

        assertThat(Json.encodeToString(deviceSession)).isEqualTo("\"$urn\"")

        deviceSession = Json.decodeFromString("\"$urn\"")
        assertThat(deviceSession.urn).isEqualTo(urn)
    }
}

