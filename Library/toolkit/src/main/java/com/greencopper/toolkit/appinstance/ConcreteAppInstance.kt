package com.greencopper.toolkit.appinstance

import android.content.Context
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.assembly.ToolkitAssembly
import com.greencopper.toolkit.di.container.Container
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.locale.toList
import com.greencopper.toolkit.locale.toLocaleList
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.multilogging.LoggingConfiguration
import com.greencopper.toolkit.logging.multilogging.MultiLoggingConfigurationsImpl
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

internal class ConcreteAppInstance private constructor(
    override val log: Logging,
    private val container: Container,
    applicationContext: Context?
) : AppInstance, Resolver by container {

    private val configuration = applicationContext?.resources?.configuration
    private var deviceLocales = configuration?.locales?.toList()

    override var date: () -> ZonedDateTime = { ZonedDateTime.now(zoneId) }

    private var forcedLocale: Locale? = null
    private var configLocales: List<Locale>? = null
    private var configFallbackLocale: Locale? = null

    override val locale: Locale
        get() = configuration?.locales?.toList()?.first() ?: Locale.getDefault()

    override fun setConfigLocale(locales: List<Locale>?, fallbackLocale: Locale?) {
        configLocales = locales
        configFallbackLocale = fallbackLocale
        formatLocaleList()
    }

    override fun setForcedLocale(locale: Locale?) {
        forcedLocale = locale
        formatLocaleList()
    }

    private fun formatLocaleList() {
        val localeList = deviceLocales?.toMutableList() ?: mutableListOf()

        configLocales?.let { locales ->
            deviceLocales?.firstOrNull { deviceLocale -> locales.any { deviceLocale.language == it.language } }
                ?: configFallbackLocale
        }?.let {
            localeList.add(0, it)
        }
        forcedLocale?.let {
            localeList.add(0, it)
        }

        configuration?.setLocales(localeList.distinct().toLocaleList())
    }

    override fun refreshLocales() {
        deviceLocales = configuration?.locales?.toList()
        formatLocaleList()
    }

    override var zoneId: ZoneId = ZoneId.systemDefault()

    internal fun assemble() {
        container.assemble()
    }

    companion object {
        internal fun create(
            assemblies: List<Assembly>,
            loggingConfigurations: List<LoggingConfiguration>,
            applicationContext: Context?
        ): ConcreteAppInstance {
            val logger = MultiLoggingConfigurationsImpl().apply {
                for (loggingConfiguration in loggingConfigurations) {
                    this.addConfiguration(loggingConfiguration)
                }
            }

//            val diLogger = if (loggingConfigurations.isEmpty()) null else logger
            // Insert diLogger as a parameter to add DI logs
            val container = Container()
            // Order matters. ToolKit needs to come first.
            container.bindAssembly(ToolkitAssembly(applicationContext))
            container.bindAssembly(*assemblies.toTypedArray())

            return ConcreteAppInstance(logger, container, applicationContext)
        }
    }
}
