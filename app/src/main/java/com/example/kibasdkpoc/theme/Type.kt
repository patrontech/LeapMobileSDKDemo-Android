package com.example.kibasdkpoc.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

public val Typography: Typography = Typography(
	bodyLarge = TextStyle(
		fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
	), titleMedium = TextStyle(
		fontWeight = FontWeight.SemiBold,
		fontSize = 16.sp,
	)
)