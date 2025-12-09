package com.greencopper.core.content.recipe

import android.content.Context
import java.io.File
import java.io.FileOutputStream

public interface RecipeOverride {
    public val componentPathOverride: String

    public fun overrideConfigExists(context: Context): Boolean = try {
        context.assets.list(componentPathOverride)?.isNotEmpty() == true
    } catch (throwable: Throwable) {
        false
    }

    public fun overrideFolder(context: Context): File {
        val customOverridesFolder = "custom-overrides/"
        copyAssets(context, componentPathOverride, customOverridesFolder)
        return File(context.cacheDir, "$customOverridesFolder$componentPathOverride")
    }

    /**
     * That is recursive function that returns true if path is file
     */
    private fun copyAssets(context: Context, path: String, customOverridesFolder: String): Boolean {
        val assetsManager = context.assets
        val filesList = assetsManager.list(path)
        return if(filesList.isNullOrEmpty()) {
            true
        } else {
            val pathWithSeparator = path.addSeparatorIfRequired()
            val assetFolder = File(context.cacheDir, "$customOverridesFolder$pathWithSeparator")
            assetFolder.mkdirs()
            filesList.forEach { file ->
                if(copyAssets(context, "$pathWithSeparator$file", customOverridesFolder)) {
                    val assetFile = File(context.cacheDir, "$customOverridesFolder$pathWithSeparator$file")
                    FileOutputStream(assetFile).use { output ->
                        assetsManager.open("$pathWithSeparator$file").use { input ->
                            input.copyTo(output)
                        }
                    }
                }
            }
            false
        }
    }

    private fun String.addSeparatorIfRequired(): String =
        if(this.endsWith("/")) {
            this
        } else {
            this.plus("/")
        }
}
