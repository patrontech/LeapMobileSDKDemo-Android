package com.greencopper.interfacekit.favorites

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import kotlinx.serialization.Serializable

@Serializable
public data class FavoritesEditing(val add: Icon, val remove: Icon) {
    @Serializable
    public data class Icon(val icon: String, val accessibilityLabel: String)
}

public fun FavoritesEditing.translate(localizationService: LocalizationService): FavoritesEditing {
    return FavoritesEditing(
        add = FavoritesEditing.Icon(
            add.icon,
            localizationService.getString(add.accessibilityLabel)
        ),
        remove = FavoritesEditing.Icon(
            remove.icon,
            localizationService.getString(remove.accessibilityLabel)
        )
    )
}
