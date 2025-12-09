package com.greencopper.interfacekit.editorial.repository

import android.net.Uri
import androidx.core.net.toUri
import com.greencopper.core.localization.service.LocalizationService
import java.io.File

internal class ConcreteEditorialPageRepository(
    private val localizationService: LocalizationService
) : EditorialPageRepository {

    private var directoryPath: String? = null

    override fun setContentDirectoryPath(path: String) {
        directoryPath = path
    }

    override fun getFileUri(editorialPageFileName: String): Uri? {
        return directoryPath?.let {
            val directory = File(it)
            val localizedFileName = localizationService.getStringFromRepository(editorialPageFileName)
                ?: editorialPageFileName
            val file = File(directory, localizedFileName)

            return if (file.exists()) file.toUri() else null
        }
    }
}