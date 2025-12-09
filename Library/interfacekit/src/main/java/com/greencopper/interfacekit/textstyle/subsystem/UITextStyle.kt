package com.greencopper.interfacekit.textstyle.subsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.greencopper.interfacekit.ui.compose.LocalTextStyleAccess
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve

public abstract class UITextStyle(private val parent: UITextStyle? = null) {
    protected abstract val level: String
    public fun getLevels(leaf: String? = null): List<String> {
        return mutableListOf<String>().apply {
            parent?.getLevels()?.let { addAll(it) }
            add(level)
            leaf?.let { add(it) }
        }
    }

    protected fun toIKFont(
        leaf: String,
        textStyle: IKFont.TextStyle = IKFont.TextStyle.bodyS,
        vararg fallbacks: IKFont,
    ): IKFont {
        return App.resolve<TextStyleRepository>().getIKFont(getLevels(leaf), textStyle, *fallbacks)
    }

    @Composable
    protected fun composeIKFont(
        leaf: String,
        textStyle: IKFont.TextStyle = IKFont.TextStyle.bodyS,
        vararg fallbacks: IKFont,
    ): TextStyle =
        LocalTextStyleAccess.current(getLevels(leaf), textStyle, listOf(*fallbacks)).toTextStyle()
}

private fun IKFont.toTextStyle(): TextStyle = TextStyle(
    fontSize = fontSize.sp,
    fontFamily = FontFamily(typeface)
)

public abstract class ScreenTextStyle(parent: UITextStyle) : UITextStyle(parent) {
    public open val topBar: TopBarTextStyle get() = TopBarTextStyle(this)
}

public abstract class SelectableTextStyle(parent: UITextStyle) : UITextStyle(parent) {
    protected abstract val normalDefault: IKFont.TextStyle
    protected abstract val selectedDefault: IKFont.TextStyle
    public val normal: TextStyle @Composable get() = composeIKFont("normal", normalDefault)
    public val selected: TextStyle @Composable get() = composeIKFont("selected", selectedDefault)
}

public class TopBarTextStyle(parent: UITextStyle) : UITextStyle(parent) {
    override val level: String = "topBar"

    public val title: Title = Title(this)

    public class Title(parent: UITextStyle): UITextStyle(parent){
        override val level: String = "title"

        public val large: IKFont get() = toIKFont("large", IKFont.TextStyle.largeTitle)
        public val normal: IKFont get() = toIKFont("normal", IKFont.TextStyle.titleS)
    }
}
