package com.greencopper.interfacekit.tags.ui

import com.greencopper.interfacekit.color
import com.greencopper.interfacekit.color.UIColor
import com.greencopper.toolkit.App

public class TagColor(parent: UIColor) : UIColor(parent) {
    override val level: String = "tags"

    public val pill: Pill = Pill(this)

    public class Pill(parent: UIColor) : UIColor(parent) {
        override val level: String = "pill"

        public val border: Int get() = App.color(getLevels("border"), default.fill.secondary)
        public val background: Int get() = App.color(getLevels("background"), default.background.secondary)
        public val label: Int get() = App.color(getLevels("label"), default.label.secondary)
    }
}
