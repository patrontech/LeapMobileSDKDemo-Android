package com.greencopper.toolkit.zip

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException

internal class ZipExceptionTest {

    @Test
    fun inputExceptionMessage() {
        val testFile = File("testDir/testFile")
        val error = ZipException.InputException(testFile)
        assertThat(
            error.message
        ).contains("[ZipException] Input ${testFile.path} is not a folder")
    }

    @Test
    fun unknownExceptionMessage() {
        val error = ZipException.UnknownException(IOException())
        assertThat(error.message).contains("[ZipException] UnknownException: ")
    }
}