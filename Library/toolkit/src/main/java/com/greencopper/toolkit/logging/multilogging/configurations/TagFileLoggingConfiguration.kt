package com.greencopper.toolkit.logging.multilogging.configurations

import com.greencopper.toolkit.logging.LogLevel
import java.io.File

public class TagFileLoggingConfiguration(
    baseDirectory: File,
    filename: String,
    private val restrictTag: String,
    isAsync: Boolean = true
) : FileLoggingConfiguration(baseDirectory, filename, isAsync) {

    private fun isLoggable(
        tag: String?
    ): Boolean {
        return tag?.contains(restrictTag, false) ?: false
    }

    override fun log(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?,
        vararg args: Any?
    ) {
        if (isLoggable(tag)) {
            super.log(priority, message, tag, throwable, *args)
        }
    }
}