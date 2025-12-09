package com.greencopper.testmocks.core

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.manager.Content
import java.io.File

public fun Content.Companion.mock(version: Int, schema: Int, project: String): Content {
    return Content(
        ContentArchive(File(""), "secret"),
        version,
        schema,
        project,
        null,
    )
}
