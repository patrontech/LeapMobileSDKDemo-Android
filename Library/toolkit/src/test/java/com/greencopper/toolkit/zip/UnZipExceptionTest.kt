package com.greencopper.toolkit.zip

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException

internal class UnZipExceptionTest {

    @Test
    fun wrongPasswordExceptionMessage() {
        val error = UnZipException.WrongPasswordException()
        assertThat(error.message).contains("[UnZipException] Wrong password")
    }

    @Test
    fun unknownExceptionMessage() {
        val error = UnZipException.UnknownException(IOException())
        assertThat(error.message).contains("[UnZipException] UnknownException: ")
    }
}