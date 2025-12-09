package com.greencopper.core.content.archive

import com.greencopper.core.content.manager.ContentSchema
import com.greencopper.core.content.manager.ContentVersion

public sealed class ArchiveOpenerException(protected val archive: ContentArchive, cause: Throwable? = null) :
    Throwable(cause) {
    public class OpenArchiveException(archive: ContentArchive, cause: Throwable?) :
        ArchiveOpenerException(archive, cause) {
        override val message: String
            get() = "[ArchiveOpenerException] Error opening archive $archive : \n $cause"
    }

    public class MalformedArchiveException(archive: ContentArchive, cause: Throwable?) :
        ArchiveOpenerException(archive, cause) {
        override val message: String
            get() = "[ArchiveOpenerException] Malformed archive $archive : \n $cause"
    }

    public class WrongSchemaException(
        private val actual: ContentSchema,
        private val expected: ContentSchema,
        archive: ContentArchive
    ) : ArchiveOpenerException(archive) {
        override val message: String
            get() = "[ArchiveOpenerException] Wrong schema in $archive, expected was: $expected, actual was: $actual"
    }

    public class WrongVersionException(
        private val actual: ContentVersion,
        private val expected: ContentVersion,
        archive: ContentArchive
    ) : ArchiveOpenerException(archive) {
        override val message: String
            get() = "[ArchiveOpenerException] Wrong version in $archive, expected was: $expected, actual was: $actual"
    }

    public class MoveArchiveException(archive: ContentArchive, cause: Throwable?) :
        ArchiveOpenerException(archive, cause) {
        override val message: String
            get() = "[ArchiveOpenerException] Error moving archive $archive: \n $cause"
    }
}