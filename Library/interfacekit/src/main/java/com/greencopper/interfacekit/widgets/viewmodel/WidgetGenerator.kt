package com.greencopper.interfacekit.widgets.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

public interface WidgetGenerator {
    public val id: String?
    public val topPadding: Int
    public val bottomPadding: Int
    public val generateComposable: @Composable (modifier: Modifier) -> Unit
}
