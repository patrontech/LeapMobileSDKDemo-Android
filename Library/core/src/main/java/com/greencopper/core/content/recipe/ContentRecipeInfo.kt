package com.greencopper.core.content.recipe

import kotlinx.serialization.Serializable

@Serializable
public data class ContentRecipeInfo(val name: ContentRecipeName, val version: ContentRecipeVersion)