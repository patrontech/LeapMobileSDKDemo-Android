package com.greencopper.toolkit.zip

public sealed class UnZipException(cause: Throwable? = null) : Throwable(cause) {
    public class WrongPasswordException : UnZipException() {
        override val message: String?
            get() = "[UnZipException] Wrong password"
    }

    public class UnknownException(cause: Throwable) : UnZipException(cause) {
        override val message: String?
            get() = "[UnZipException] UnknownException: ${super.message}"
    }
}
