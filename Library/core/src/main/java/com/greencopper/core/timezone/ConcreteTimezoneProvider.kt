package com.greencopper.core.timezone

import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.toolkit.App
import java.time.ZoneId

public class ConcreteTimezoneProvider(
    private val coreConfigHolder: CoreConfigurationHolder
) : TimezoneProvider {

    override val zoneId: ZoneId
        get() = coreConfigHolder.currentConfiguration.value
            ?.timezone?.let { ZoneId.of(it) }
            ?: App.zoneId
}