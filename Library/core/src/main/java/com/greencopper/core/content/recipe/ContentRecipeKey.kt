package com.greencopper.core.content.recipe

import com.greencopper.core.content.Key
import kotlinx.serialization.Serializable

internal typealias ContentRecipeName = String
internal typealias ContentRecipeVersion = Int

@Serializable
public data class ContentRecipeKey(
    override val name: ContentRecipeName,
    override val version: ContentRecipeVersion,
    val implementation: Int? = null,
) : Key() {
    val info: ContentRecipeInfo = ContentRecipeInfo(name, version)
}