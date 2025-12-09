package com.greencopper.testmocks.toolkit

import com.greencopper.toolkit.logging.LogLevel
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.multilogging.LoggingConfiguration

public class MockLogging : Logging {
    override fun addConfiguration(vararg loggingConfigurations: LoggingConfiguration): Unit = Unit

    override fun removeConfiguration(loggingConfiguration: LoggingConfiguration): Unit = Unit

    override fun removeAllConfigurations(): Unit = Unit

    override fun configurations(): List<LoggingConfiguration> = emptyList()

    public var lastPriority: LogLevel? = null
    public var lastMessage: String? = null
    public var lastTag: String? = null
    public var lastThrowable: Throwable? = null

    override fun log(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?,
        vararg args: Any?,
    ) {
        lastPriority = priority
        lastMessage = message
        lastTag = tag
        lastThrowable = throwable

        println("$tag: $priority: $message")
        throwable?.printStackTrace()
    }
}
