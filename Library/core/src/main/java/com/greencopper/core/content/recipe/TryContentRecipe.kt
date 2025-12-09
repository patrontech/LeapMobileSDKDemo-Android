package com.greencopper.core.content.recipe

import com.greencopper.core.content.manager.ContentException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

public interface TryContentRecipe : ContentRecipe {
    public suspend fun tryToProcess(unarchivedDirectory: File, contentDirectory: File)

    public suspend fun tryToApply(contentDirectory: File)

    override suspend fun process(unarchivedDirectory: File, contentDirectory: File) {
        tryToExecute { tryToProcess(unarchivedDirectory, contentDirectory) }
    }

    override suspend fun apply(contentDirectory: File) {
        tryToExecute { tryToApply(contentDirectory) }
    }

    private suspend fun tryToExecute(functionToExecute: suspend () -> Unit) {
        try {
            functionToExecute()
        } catch (error: Throwable) {
            throw ContentException.RecipeException(error, this@TryContentRecipe)
        }
    }
}
