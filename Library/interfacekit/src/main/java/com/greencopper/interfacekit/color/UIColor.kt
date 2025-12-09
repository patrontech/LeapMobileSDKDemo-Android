package com.greencopper.interfacekit.color

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.ui.compose.LocalColorAccess
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import androidx.compose.ui.graphics.Color as ColorComposable

public abstract class UIColor(private val parent: UIColor? = null) {
    public abstract val level: String
    public fun getLevels(leaf: String? = null): List<String> {
        return mutableListOf<String>().apply {
            parent?.getLevels()?.let { addAll(it) }
            add(level)
            leaf?.let { add(it) }
        }
    }

    @Composable
    public fun composeColor(leaf: String, color: () -> Color): ColorComposable {
        return LocalColorAccess.current(getLevels(leaf), color)
    }

    public companion object {
        public val default: DefaultColors
            get() = App.resolve<ColorRepository>().getDefaultColors()
    }
}

public fun Int.removeAlpha(): Int = or(0xFF000000.toInt())

public fun isDarkMode(): Boolean {
    val resources = App.resolve<Context>().resources
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        else -> false
    }
}

public fun getCheckableColor(normal: Int, checked: Int): ColorStateList {
    val colors = intArrayOf(normal, checked)
    val states = arrayOf(
        intArrayOf(-android.R.attr.state_checked),
        intArrayOf(android.R.attr.state_checked),
    )
    return ColorStateList(states, colors)
}

public fun getPressableColor(normal: Int, pressed: Int): ColorStateList {
    val colors = intArrayOf(normal, pressed)
    val states = arrayOf(
        intArrayOf(-android.R.attr.state_pressed),
        intArrayOf(android.R.attr.state_pressed)
    )
    return ColorStateList(states, colors)
}

public fun String.parseRGBA(): Int {
    val cleanHex = this.removePrefix("#") // Remove '#'if present
    if (cleanHex.length != 8) {
        throw IllegalArgumentException("Invalid RGBA string format.")
    }

    val red = cleanHex.substring(0, 2).toInt(16)
    val green = cleanHex.substring(2, 4).toInt(16)
    val blue = cleanHex.substring(4, 6).toInt(16)
    val alpha = cleanHex.substring(6, 8).toInt(16)

    return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
