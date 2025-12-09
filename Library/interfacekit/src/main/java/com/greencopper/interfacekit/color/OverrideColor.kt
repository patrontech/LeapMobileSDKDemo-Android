package com.greencopper.interfacekit.color

import android.content.res.ColorStateList
import androidx.compose.runtime.Composable
import com.greencopper.interfacekit.color
import com.greencopper.interfacekit.statusBarColor
import com.greencopper.toolkit.App
import kotlinx.serialization.Serializable
import androidx.compose.ui.graphics.Color as ColorCompose

@Serializable
public data class OverrideColor(val light: String? = null, val dark: String? = null)

public fun OverrideColor.toColorInt(): Int? =
    if (isDarkMode()) {
        dark?.parseToColor() ?: light?.parseToColor()
    } else {
        light?.parseToColor()
    }

@Serializable
public data class OverrideStatusBar(
    val light: DefaultColors.StatusBar.Style? = null,
    val dark: DefaultColors.StatusBar.Style? = null
)

public abstract class SelectableColor(parent: UIColor) : UIColor(parent) {
    protected abstract val normalDefault: Color
    protected abstract val selectedDefault: Color
    public val normal: Int get() = App.color(getLevels("normal"), normalDefault)
    public val selected: Int get() = App.color(getLevels("selected"), selectedDefault)

    public fun toColorStateList(): ColorStateList = getCheckableColor(normal, selected)
}

public abstract class SelectableColorComposable(parent: UIColor) : UIColor(parent) {
    protected abstract val normalDefault: () -> Color
    protected abstract val selectedDefault: () -> Color
    public val normal: ColorCompose @Composable get() = composeColor("normal", normalDefault)
    public val selected: ColorCompose @Composable get() = composeColor("selected", selectedDefault)
}

public abstract class PressableColor(parent: UIColor) : UIColor(parent) {
    protected abstract val normalDefault: Color
    protected abstract val pressedDefault: Color
    public val normal: Int get() = App.color(getLevels("normal"), normalDefault)
    public val pressed: Int get() = App.color(getLevels("pressed"), pressedDefault)

    public fun toColorStateList(): ColorStateList = getPressableColor(normal, pressed)
}

public abstract class PressableColorComposable(parent: UIColor) : UIColor(parent) {
    protected abstract val normalDefault: () -> Color
    protected abstract val pressedDefault: () -> Color
    public val normal: ColorCompose @Composable get() = composeColor("normal", normalDefault)
    public val pressed: ColorCompose @Composable get() = composeColor("pressed", pressedDefault)
}

public abstract class ScreenColor(
    parent: UIColor,
) : UIColor(parent) {
    protected open val backgroundDefault: Color get() = default.background.primary
    protected open val statusBarDefault: DefaultColors.StatusBar get() = default.statusBar
    public val background: Int get() = App.color(getLevels("background"), backgroundDefault)
    public val backgroundComposable: ColorCompose @Composable get() = composeColor("background") { backgroundDefault }
    public val statusBar: DefaultColors.StatusBar
        get() = App.statusBarColor(
            getLevels("statusBar"),
            statusBarDefault
        )
    public open val topBar: TopBarColor get() = TopBarColor(this)
    public open val topBarComposable: TopBarColorComposable get() = TopBarColorComposable(this)
}

public class TopBarColor(
    parent: ScreenColor,
    private val defaultBackground: Color = default.topBar.background,
    private val defaultTitle: Color = default.topBar.title,
    private val defaultItem: Color = default.topBar.item
) : UIColor(parent) {
    override val level: String = "topBar"
    public val background: Int get() = App.color(getLevels("background"), defaultBackground)
    public val title: Int get() = App.color(getLevels("title"), defaultTitle)
    public val item: Int get() = App.color(getLevels("item"), defaultItem)
}

public class TopBarColorComposable(
    parent: ScreenColor,
    private val defaultBackground: Color = default.topBar.background,
    private val defaultTitle: Color = default.topBar.title,
    private val defaultItem: Color = default.topBar.item,
) : UIColor(parent) {
    override val level: String = "topBar"
    public val background: ColorCompose @Composable get() = composeColor("background") { defaultBackground }
    public val title: ColorCompose @Composable get() = composeColor("title") { defaultTitle }
    public val item: ColorCompose @Composable get() = composeColor("item") { defaultItem }
}

