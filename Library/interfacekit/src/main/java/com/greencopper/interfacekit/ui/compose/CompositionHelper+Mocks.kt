package com.greencopper.interfacekit.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.graphics.Color
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.color.Color as IKColor

@Composable
public fun mockColors(
    map: Map<List<String>, Color> = mapOf(),
    failIfMissing: Boolean = false,
): ProvidedValue<(List<String>, () -> IKColor) -> Color> {
    return LocalColorAccess provides { levels, default ->
        map[levels] ?: if (failIfMissing) {
            error("Color `${levels.joinToString(("."))}` not mocked")
        } else {
            Color.Black
        }
    }
}

@Composable
public fun mockTextStyle(
    map: Map<List<String>, IKFont> = mapOf(),
    failIfMissing: Boolean = false,
): ProvidedValue<(List<String>, IKFont.TextStyle, List<IKFont>) -> IKFont> {
    return LocalTextStyleAccess provides { levels, textStyle, _ ->
        map[levels] ?: if (failIfMissing) {
            error("TextStyle `${levels.joinToString(("."))}` not mocked")
        } else {
            IKFont(textStyle)
        }
    }
}

@Composable
public fun mockStrings(
    map: Map<String, String> = mapOf(),
    failIfMissing: Boolean = false,
): ProvidedValue<LocalizationService> {
    fun getString(key: String): String = map[key] ?: if (failIfMissing) {
        error("Localized String `$key` not mocked")
    } else {
        "foo"
    }
    return LocalLocalizationAccess provides object : LocalizationService {
        override fun getStringFromRepository(key: String): String =
            getString(key)

        override fun getDefaultLocaleString(key: String): String =
            getString(key)

        override fun getQuantityStringFromRepository(key: String, quantity: Int): String =
            getString(key)
    }
}
