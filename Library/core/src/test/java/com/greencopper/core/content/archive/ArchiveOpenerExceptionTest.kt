package com.greencopper.core.content.archive

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

internal class ArchiveOpenerExceptionTest {

    private val contentArchive = ContentArchive(File("archiveFile"), "secret")

    @Test
    fun openArchiveExceptionMessage() {
        val exception = ArchiveOpenerException.OpenArchiveException(
            contentArchive,
            IllegalArgumentException()
        )
        Assertions.assertThat(exception).hasMessage(
            "[ArchiveOpenerException] Error opening archive $contentArchive : \n" +
                " java.lang.IllegalArgumentException"
        )
    }

    @Test
    fun malformedArchiveExceptionMessage() {
        val exception = ArchiveOpenerException.MalformedArchiveException(
            contentArchive,
            java.lang.IllegalArgumentException()
        )
        Assertions.assertThat(exception).hasMessage(
            "[ArchiveOpenerException] Malformed archive $contentArchive : \n" +
                " java.lang.IllegalArgumentException"
        )
    }

    @Test
    fun wrongSchemaExceptionMessage() {
        val exception = ArchiveOpenerException.WrongSchemaException(5, 6, contentArchive)
        Assertions.assertThat(exception).hasMessage(
            "[ArchiveOpenerException] Wrong schema in $contentArchive, expected was: 6, actual was: 5"
        )
    }

    @Test
    fun wrongVersionExceptionMessage() {
        val exception = ArchiveOpenerException.WrongVersionException(5, 6, contentArchive)
        Assertions.assertThat(exception).hasMessage(
            "[ArchiveOpenerException] Wrong version in $contentArchive, expected was: 6, actual was: 5"
        )
    }

    @Test
    fun moveArchiveExceptionMessage() {
        val exception = ArchiveOpenerException.MoveArchiveException(
            contentArchive,
            java.lang.IllegalArgumentException()
        )
        Assertions.assertThat(exception).hasMessage(
            "[ArchiveOpenerException] Error moving archive $contentArchive: \n" +
                " java.lang.IllegalArgumentException"
        )
    }
}