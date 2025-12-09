package com.greencopper.interfacekit.topbar

import androidx.annotation.DrawableRes
import com.greencopper.interfacekit.ui.views.navigationcontrols.KibaToolbar
import kotlinx.serialization.Serializable

@Serializable
public data class TopBarState<Action>(
    val buttons: List<TopBarButton<Action>>,
) {
    @Serializable
    public data class TopBarButton<Action>(
        val title: String?,
        val icon: String?,
        @DrawableRes val iconResource: Int?,
        val side: KibaToolbar.Side,
        val shouldColor: Boolean = true,
        val accessibilityLabel: String? = null,
        val id: Int? = null,
        val onClick: Action,
    )
}
