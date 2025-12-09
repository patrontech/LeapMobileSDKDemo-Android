package com.greencopper.core.timezone

import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneId

internal class ConcreteTimezoneProviderTest {

    private val coreConfigHolder = CoreConfigurationHolder()
    private val provider = ConcreteTimezoneProvider(coreConfigHolder)

    init {
        Toolkit.setupTest()
    }

    @Test
    fun withNullConfig_zoneId_returnsAppZoneId() {
        coreConfigHolder.currentConfiguration.value = null
        assertThat(provider.zoneId).isEqualTo(App.zoneId)
    }

    @Test
    fun configWithNullTimezone_zoneId_returnsAppZoneId() {
        coreConfigHolder.currentConfiguration.value = CoreConfiguration(
            CoreConfiguration.RemoteState("", 1),
            null,
            CoreConfiguration.OTA(""),
            timezone = null,
            CoreConfiguration.ContentConfig(60, listOf()),
        )

        assertThat(provider.zoneId).isEqualTo(App.zoneId)
    }

    @Test
    fun configWithTimezone_zoneId_returnsZoneId() {
        val timezone = "America/Montreal"

        coreConfigHolder.currentConfiguration.value = CoreConfiguration(
            CoreConfiguration.RemoteState("", 0),
            null,
            CoreConfiguration.OTA(""),
            timezone = timezone,
            CoreConfiguration.ContentConfig(60, listOf())
        )

        assertThat(provider.zoneId).isEqualTo(ZoneId.of(timezone))
    }
}