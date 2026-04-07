package com.example.kibasdkpoc.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

public val Purple80: Color = Color(0xFFD0BCFF)
public val PurpleGrey80: Color = Color(0xFFCCC2DC)
public val Pink80: Color = Color(0xFFEFB8C8)

public val Purple40: Color = Color(0xFF6650a4)
public val PurpleGrey40: Color = Color(0xFF625b71)
public val Pink40: Color = Color(0xFF7D5260)

@Composable
public fun attrColorResource(attrId: Int): Color {
    val context = LocalContext.current
    val attrs = context.theme.obtainStyledAttributes(intArrayOf(attrId))
    val colorValue = attrs.getColor(0, 0)
    attrs.recycle()
    return Color(colorValue)
}