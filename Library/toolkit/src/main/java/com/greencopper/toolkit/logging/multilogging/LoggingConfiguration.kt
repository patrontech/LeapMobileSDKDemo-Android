package com.greencopper.toolkit.logging.multilogging

import com.greencopper.toolkit.logging.LogLevel

/** A facade for handling logging calls. Install instances via [`Logging.configure()`][.configure]. */
public interface LoggingConfiguration {

    /** Log at `priority` a message with optional tag and exception. */
    public fun log(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?,
        vararg args: Any?
    )
}