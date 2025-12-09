package com.greencopper.core.content.archive

import com.greencopper.core.content.serializers.FileSerializer
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
public data class ContentArchive(
    @Serializable(with = FileSerializer::class) val file: File,
    internal val secret: String
) {
    override fun toString(): String {
        val className = this::class.simpleName
        return "$className(file=$file)"
    }
}