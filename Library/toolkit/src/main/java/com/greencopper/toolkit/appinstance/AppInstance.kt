package com.greencopper.toolkit.appinstance

import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.logging.Logging
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

public interface AppInstance : Resolver {
    public val log: Logging
    public var date: () -> ZonedDateTime
    public val locale: Locale
    public var zoneId: ZoneId

    /**
     * Set a Locale to use before configs and user's device Locales
     * @param locale The forced Locale, or null to remove it
     */
    public fun setForcedLocale(locale: Locale?)

    /**
     * Set a Locale to use before user's device Locales
     */
    public fun setConfigLocale(locales: List<Locale>?, fallbackLocale: Locale?)

    /**
     * Refresh the list of locales in case of config change by the user
     */
    public fun refreshLocales()
}
