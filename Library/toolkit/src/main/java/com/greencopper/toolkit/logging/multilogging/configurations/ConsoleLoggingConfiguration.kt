package com.greencopper.toolkit.logging.multilogging.configurations

import android.util.Log
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.LogLevel
import com.greencopper.toolkit.versionprovider.BuildConfigProvider

/** A [FormattedLoggingConfiguration] for debug builds. Automatically infers the tag from the calling class. */
public open class ConsoleLoggingConfiguration : FormattedLoggingConfiguration() {
    private val buildConfigProvider: BuildConfigProvider by lazy { App.resolve() }

    override fun logFormattedToDestination(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?
    ) {
        if(!buildConfigProvider.isDebug) {
            return
        }

        if (message.length < MAX_LOG_LENGTH) {
            if (priority == LogLevel.ASSERT) {
                Log.wtf(tag, message)
            } else {
                Log.println(priority.value, tag, message)
            }
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        message.chunked(MAX_LOG_LENGTH).forEach { chunk ->
            if (priority == LogLevel.ASSERT) {
                Log.wtf(tag, "$chunk \n")
            } else {
                Log.println(priority.value, tag, "$chunk \n")
            }
        }
    }

    public companion object {
        private const val MAX_LOG_LENGTH = 4000
    }
}
