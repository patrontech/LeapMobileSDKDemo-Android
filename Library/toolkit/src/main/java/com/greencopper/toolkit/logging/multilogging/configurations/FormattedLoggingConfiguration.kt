package com.greencopper.toolkit.logging.multilogging.configurations

import com.greencopper.toolkit.logging.LogLevel
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.multilogging.LoggingConfiguration
import com.greencopper.toolkit.logging.multilogging.MultiLoggingConfigurationsImpl
import java.io.PrintWriter
import java.io.StringWriter

/** Abstract class used to provide a better formatted string to the underlying implementations*/
public abstract class FormattedLoggingConfiguration :
    LoggingConfiguration {
    private val explicitTag = ThreadLocal<String>()

    private val ignoredOrigins = listOf(
        Logging::class.java.name,
        "${Logging::class.java.name}Kt",
        "${Logging::class.java.name}\$DefaultImpls",
        MultiLoggingConfigurationsImpl::class.java.name,
        FormattedLoggingConfiguration::class.java.name,
        ConsoleLoggingConfiguration::class.java.name,
        FileLoggingConfiguration::class.java.name,
        TagFileLoggingConfiguration::class.java.name
    )

    /**
     * Extract the tag which should be used for the message from the `element`. By default
     * this will use the class name without any anonymous class suffixes (e.g., `Foo$1`
     * becomes `Foo`).
     *
     * Note: This will not be called if a [manual tag][.tag] was specified.
     */
    private fun getFormattedStackTrace(): String {
        val element = Throwable().stackTrace
            .first { it.className !in ignoredOrigins }
        var className = element.className.substringAfterLast(".")
        className = className.replace(ANONYMOUS_CLASS, "")
        return "$className:${element.methodName}:${element.lineNumber}:"
    }

    override fun log(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?,
        vararg args: Any?
    ) {
        var logMessage = message
        if (logMessage.isEmpty()) {
            if (throwable == null) {
                return  // Swallow message if it's null and there's no throwable.
            }
            logMessage = getStackTraceString(throwable)
        } else {
            if (args.isNotEmpty()) {
                logMessage = formatMessage(logMessage, args)
            }
            if (throwable != null) {
                logMessage += "\n${getStackTraceString(throwable)}"
            }
        }
        var prefixTag = getFormattedStackTrace()
        tag?.let { prefixTag += "TAG:$it " }
        logFormattedToDestination(priority, logMessage, prefixTag, throwable)
    }

    /** Formats a log message with optional arguments. */
    protected open fun formatMessage(message: String, args: Array<out Any?>): String =
        message.format(*args)

    private fun getStackTraceString(t: Throwable): String {
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    protected abstract fun logFormattedToDestination(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable? = null
    )

    public companion object {
        private val ANONYMOUS_CLASS = Regex("(\\$\\d+)+$")
    }
}

