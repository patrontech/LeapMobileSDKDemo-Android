package com.greencopper.toolkit.logging.multilogging

import com.greencopper.toolkit.logging.LogLevel
import com.greencopper.toolkit.logging.Logging

internal class MultiLoggingConfigurationsImpl : Logging {

    private val configurations = mutableListOf<LoggingConfiguration>()

    /** Adds new logging configurations. */
    override fun addConfiguration(vararg loggingConfigurations: LoggingConfiguration) {
        synchronized(configurations) {
            configurations.addAll(
                loggingConfigurations
            )
        }
    }

    /** Remove a configured configuration. */
    override fun removeConfiguration(loggingConfiguration: LoggingConfiguration) {
        synchronized(configurations) {
            require(configurations.remove(loggingConfiguration))
            { "Cannot remove configuration which is not configured: $loggingConfiguration" }
        }
    }

    /** Remove all configured configurations. */
    override fun removeAllConfigurations() {
        synchronized(configurations) {
            configurations.clear()
        }
    }

    /** Return a copy of all configured [configurations][LoggingConfiguration]. */
    override fun configurations(): List<LoggingConfiguration> {
        synchronized(configurations) {
            return configurations.toList()
        }
    }

    override fun log(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?,
        vararg args: Any?
    ) {
        configurations.forEach { config -> config.log(priority, message, tag, throwable, *args) }
    }
}