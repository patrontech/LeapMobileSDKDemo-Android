package com.greencopper.toolkit.logging.multilogging.configurations

import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.LogLevel
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.logging.multilogging.LoggingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public open class FileLoggingConfiguration(
    baseDirectory: File,
    fileName: String,
    private val isAsync: Boolean = true
) : FormattedLoggingConfiguration() {

    public val file: File = File(baseDirectory, "$fileName.log")
    private val scope = CoroutineScope(
        Dispatchers.IO
    )

    init {
        if (!baseDirectory.exists()) {
            baseDirectory.mkdir()
        } else if (!baseDirectory.isDirectory) {
            throw LoggingException.IOInputException()
        }
        if (!file.exists()) {
            file.createNewFile()
        } else if (!file.isFile) {
            throw LoggingException.IOInputException()
        }
    }

    override fun logFormattedToDestination(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?
    ) {
        if (isAsync) {
            scope.launch {
                logToFile(priority, message, tag)
            }
        } else {
            logToFile(priority, message, tag)
        }
    }

    private fun logToFile(
        priority: LogLevel,
        message: String,
        tag: String?
    ) {
        if (file.canWrite()) {
            val dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"))

            val baseString = "$dateString : ${priority.name} - $tag : $message\n"
            file.appendText("$baseString \n")
        } else {
            App.log.e(this.javaClass.simpleName, "Error while writing logs to file ${file.path}")
        }
    }
}
