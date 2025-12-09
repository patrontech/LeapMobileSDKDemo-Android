package com.greencopper.interfacekit.empty.ui

import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.textstyle.subsystem.UITextStyle

public class EmptyViewTextStyles(parent: UITextStyle) : UITextStyle(parent) {
    override val level: String = "empty"

    internal val title get() = toIKFont("title", IKFont.TextStyle.titleL)
    internal val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.bodyM)
}
