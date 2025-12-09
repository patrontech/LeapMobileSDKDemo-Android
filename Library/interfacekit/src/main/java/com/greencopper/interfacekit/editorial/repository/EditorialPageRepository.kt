package com.greencopper.interfacekit.editorial.repository

import android.net.Uri

public interface EditorialPageRepository {

    public fun setContentDirectoryPath(path: String)

    public fun getFileUri(editorialPageFileName: String): Uri?
}