package com.greencopper.interfacekit.editorial.recipe

import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.TryContentRecipe
import com.greencopper.interfacekit.editorial.repository.EditorialPageRepository
import java.io.File

public open class EditorialPageRecipe(
    private val repository: EditorialPageRepository
) : TryContentRecipe {

    override val key: ContentRecipeKey = ContentRecipeKey("InterfaceKit.EditorialPage", 1, 1)
    override val componentPath: String = "interfaceKit/editorialPage"

    override suspend fun tryToProcess(unarchivedDirectory: File, contentDirectory: File) {
        require(unarchivedDirectory.isDirectory) { "Unarchived directory is not a proper directory" }
        unarchivedDirectory.copyRecursively(contentDirectory, overwrite = true)
    }

    override suspend fun tryToApply(contentDirectory: File) {
        repository.setContentDirectoryPath(contentDirectory.path)
    }
}
