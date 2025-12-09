package com.greencopper.interfacekit.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.color.Color as IKColor

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
public fun MainCompositionLocalProvider(
    vararg values: ProvidedValue<*>, content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        *values,
        LocalOverscrollConfiguration provides null,
        LocalRippleConfiguration provides null,
    ) {
        content()
    }
}

public val LocalColorAccess: ProvidableCompositionLocal<(List<String>, () -> IKColor) -> Color> =
    compositionLocalOf {
        { _, _ -> Color.Black }
    }

public val LocalTextStyleAccess: ProvidableCompositionLocal<(List<String>, IKFont.TextStyle, List<IKFont>) -> IKFont> =
    compositionLocalOf {
        { _, _, _ ->
            IKFont(IKFont.TextStyle.bodyS, listOf())
        }
    }

public val LocalLocalizationAccess: ProvidableCompositionLocal<LocalizationService> =
    compositionLocalOf {
        error("LocalizationService isn't available in this Composition")
    }

public val LocalImageAccess: ProvidableCompositionLocal<ImageService> = compositionLocalOf {
    error("ImageService isn't available in this Composition")
}
