package com.greencopper.toolkit.logging.multilogging

public sealed class LoggingException : Throwable() {
    public class IOInputException : LoggingException() {
        override val message: String?
            get() = "[LoggingException] IO Error with file parameters"
    }
}