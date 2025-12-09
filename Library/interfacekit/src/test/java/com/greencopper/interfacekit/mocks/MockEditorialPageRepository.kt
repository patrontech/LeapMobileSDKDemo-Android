package com.greencopper.interfacekit.mocks

import android.net.Uri
import com.greencopper.interfacekit.editorial.repository.EditorialPageRepository

internal class MockEditorialPageRepository(
    private val setContentDirectoryPath: (String) -> Unit = { },
    private val getFileUri: () -> Uri? = { null }
) : EditorialPageRepository {

    override fun setContentDirectoryPath(path: String) = setContentDirectoryPath.invoke(path)

    override fun getFileUri(editorialPageFileName: String): Uri? = getFileUri()
}
