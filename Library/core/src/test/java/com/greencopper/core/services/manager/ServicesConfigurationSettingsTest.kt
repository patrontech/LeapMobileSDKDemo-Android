package com.greencopper.core.services.manager

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ServicesConfigurationSettingsTest {
    private companion object {
        const val START_JSON = "[{\"start\":\"atLaunch\"},{\"start\":\"inRestrictedArea\"},{\"start\":\"outsideRestrictedArea\"},{\"start\":\"byUser\"}]"
    }

    init {
        Toolkit.setupTest()
    }

    @Test
    fun deserializing_withStart_succeeds() {
        val decoder: Json = App.resolve()
        val decoded: List<ServicesConfiguration.Settings> =
            decoder.decodeFromString(START_JSON)
        assertThat(decoded).isEqualTo(listOf(
            ServicesConfiguration.Settings(ServicesConfiguration.Start.AT_LAUNCH),
            ServicesConfiguration.Settings(ServicesConfiguration.Start.IN_RESTRICTED_AREA),
            ServicesConfiguration.Settings(ServicesConfiguration.Start.OUTSIDE_RESTRICTED_AREA),
            ServicesConfiguration.Settings(ServicesConfiguration.Start.BY_USER)
        ))
    }

    @Test
    fun serialization_succeeds() {
        val encoder: Json = App.resolve()
        val encodable = listOf(
            ServicesConfiguration.Settings(ServicesConfiguration.Start.AT_LAUNCH),
            ServicesConfiguration.Settings(ServicesConfiguration.Start.IN_RESTRICTED_AREA),
            ServicesConfiguration.Settings(ServicesConfiguration.Start.OUTSIDE_RESTRICTED_AREA),
            ServicesConfiguration.Settings(ServicesConfiguration.Start.BY_USER)
        )
        val encoded = encoder.encodeToString(encodable)
        assertThat(encoded).isEqualTo(START_JSON)
    }
}
