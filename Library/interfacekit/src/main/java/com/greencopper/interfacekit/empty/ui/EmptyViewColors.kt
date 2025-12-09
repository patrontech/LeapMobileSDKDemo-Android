package com.greencopper.interfacekit.empty.ui

import com.greencopper.interfacekit.color
import com.greencopper.interfacekit.color.UIColor
import com.greencopper.toolkit.App

public class EmptyViewColors(parent: UIColor) : UIColor(parent) {
    override val level: String = "empty"
    internal val title get() = App.color(getLevels("title"), default.label.primary)
    internal val subtitle get() = App.color(getLevels("subtitle"), default.label.secondary)
}
