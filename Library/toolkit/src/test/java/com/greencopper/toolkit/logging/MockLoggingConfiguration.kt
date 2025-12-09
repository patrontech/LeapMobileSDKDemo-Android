package com.greencopper.toolkit.logging

import com.greencopper.toolkit.logging.multilogging.configurations.FormattedLoggingConfiguration

internal class MockLoggingConfiguration : FormattedLoggingConfiguration() {

    var logContent: String = ""
    var executed = false

    private fun isLoggable(tag: String?): Boolean {
        return !(tag?.contains("DontLog") ?: false)
    }

    override fun logFormattedToDestination(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?
    ) {
        executed = true
        if (isLoggable(tag)) {
            logContent += "Priority: ${priority.name} - Tag: $tag - Message: $message \n"
            if(throwable != null) {
                logContent += "Throwable: $throwable"
            }
        }
    }
}