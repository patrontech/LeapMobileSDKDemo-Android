package com.greencopper.toolkit.zip

import java.io.File

public sealed class ZipException(cause: Throwable? = null) : Throwable(cause) {

    public class UnknownException(cause: Throwable) : ZipException(cause) {
        override val message: String?
            get() = "[ZipException] UnknownException: ${super.message}"
    }

    public class InputException(private val originDirectory: File) : ZipException() {
        override val message: String?
            get() = "[ZipException] Input ${originDirectory.path} is not a folder"
    }
}