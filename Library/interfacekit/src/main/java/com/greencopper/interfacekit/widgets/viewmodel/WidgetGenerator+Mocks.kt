package com.greencopper.interfacekit.widgets.viewmodel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greencopper.interfacekit.ui.utils.createRect

public class MockWidgetGenerator(
    override val id: String? = null,
    override val topPadding: Int = 0,
    override val bottomPadding: Int = 0,
) : WidgetGenerator {
    override val generateComposable: @Composable ((modifier: Modifier) -> Unit) = { modifier ->
        Box(modifier = modifier.padding(top = topPadding.dp, bottom = bottomPadding.dp)) {
            createRect(width = 100.dp, height = 100.dp, color = Color.LightGray)
        }
    }
}
