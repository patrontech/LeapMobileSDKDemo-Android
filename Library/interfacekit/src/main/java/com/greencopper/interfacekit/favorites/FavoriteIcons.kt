package com.greencopper.interfacekit.favorites

import kotlinx.serialization.Serializable

@Serializable
public data class FavoriteIcons(
    val addIcon: String,
    val addAccessibilityLabel: String,
    val removeIcon: String,
    val removeAccessibilityLabel: String,
)

public fun FavoritesEditing.toFavoriteIcons(): FavoriteIcons = FavoriteIcons(
    addIcon = add.icon,
    addAccessibilityLabel = add.accessibilityLabel,
    removeIcon = remove.icon,
    removeAccessibilityLabel = remove.accessibilityLabel
)
