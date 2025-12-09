package com.greencopper.toolkit.logging

import com.greencopper.toolkit.logging.multilogging.LoggingConfiguration

public interface Logging {

    /** Adds new logging configurations. */
    public fun addConfiguration(vararg loggingConfigurations: LoggingConfiguration)

    /** Remove a configured configuration. */
    public fun removeConfiguration(loggingConfiguration: LoggingConfiguration)

    /** Remove all configured configurations. */
    public fun removeAllConfigurations()

    /** Return a copy of all configured [configurations][LoggingConfiguration]. */
    public fun configurations(): List<LoggingConfiguration>

    /** Log at `priority` a message with optional tag and exception */
    public fun log(
        priority: LogLevel,
        message: String,
        tag: String? = null,
        throwable: Throwable? = null,
        vararg args: Any?
    )
}

/** Log an exception message with optional tag, exception and format args. */
public fun Logging.e(
    message: String,
    tag: String? = null,
    throwable: Throwable? = null,
    vararg args: Any?
) {
    log(LogLevel.ERROR, message, tag, throwable, *args)
}

/** Log a warning message with optional tag, exception and format args. */
public fun Logging.w(
    message: String,
    tag: String? = null,
    throwable: Throwable? = null,
    vararg args: Any?
) {
    log(LogLevel.WARN, message, tag, throwable, *args)
}

/** Log an info message with optional tag, exception and format args. */
public fun Logging.i(
    message: String,
    tag: String? = null,
    throwable: Throwable? = null,
    vararg args: Any?
) {
    log(LogLevel.INFO, message, tag, throwable, *args)
}

/** Log a debug message with optional tag, exception and format args. */
public fun Logging.d(
    message: String,
    tag: String? = null,
    throwable: Throwable? = null,
    vararg args: Any?
) {
    log(LogLevel.DEBUG, message, tag, throwable, *args)
}

/** Log a verbose message with optional tag, exception and format args. */
public fun Logging.v(
    message: String,
    tag: String? = null,
    throwable: Throwable? = null,
    vararg args: Any?
) {
    log(LogLevel.VERBOSE, message, tag, throwable, *args)
}

public enum class LogLevel(public val value: Int) {
    VERBOSE(2),
    DEBUG(3),
    INFO(4),
    WARN(5),
    ERROR(6),
    ASSERT(7)
}