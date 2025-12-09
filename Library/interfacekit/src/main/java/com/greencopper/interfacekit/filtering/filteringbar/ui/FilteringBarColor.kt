package com.greencopper.interfacekit.filtering.filteringbar.ui

import com.greencopper.interfacekit.color
import com.greencopper.interfacekit.color.*
import com.greencopper.toolkit.App

public class FilteringBarColor(parent: UIColor) : UIColor(parent) {

    override val level: String = "filters"

    public val background: Int get() = App.color(getLevels("background"), default.background.primary)
    public val border: Int get() = App.color(getLevels("border"), default.fill.secondary)

    public val button: Button = Button(this)

    public class Button(parent: FilteringBarColor) : UIColor(parent) {
        override val level: String = "filter"

        public val title: Name get() = Name(this)
        public val background: Background get() = Background(this)
        public val border: Border get() = Border(this)

        public class Name(parent: Button) : SelectableColor(parent) {
            override val level: String = "name"
            override val normalDefault: Color = default.label.secondary
            override val selectedDefault: Color = default.label.senary
        }

        public class Background(parent: Button) : SelectableColor(parent) {
            override val level: String = "background"
            override val normalDefault: Color = default.background.secondary
            override val selectedDefault: Color = default.accent.primary
        }

        public class Border(parent: Button) : SelectableColor(parent) {
            override val level: String = "border"
            override val normalDefault: Color = default.fill.secondary
            override val selectedDefault: Color = default.accent.secondary
        }
    }
}
