package com.greencopper.interfacekit.ui.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.color.toColorInt
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository

public data class IKViewBuilder(
    public val colorRepository: ColorRepository,
    public val textStyleRepository: TextStyleRepository,
    public val localizationService: LocalizationService,
    public val imageService: ImageService,
) {
    @SuppressLint("ComposableNaming")
    @Composable
    public fun buildContent(content: @Composable () -> Unit) {
        MainCompositionLocalProvider(
            LocalColorAccess provides { levels, default ->
                Color(colorRepository.getOverrideColorInt(levels) ?: default().toColorInt())
            },
            LocalTextStyleAccess provides { levels, fallback, overrides ->
                textStyleRepository.getIKFont(levels, fallback, *overrides.toTypedArray())
            },
            LocalLocalizationAccess provides localizationService,
            LocalImageAccess provides imageService,
        ) {
            content()
        }
    }
}
