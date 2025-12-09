package com.greencopper.interfacekit.filtering.filteringbar.ui

import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.textstyle.subsystem.UITextStyle

public class FilteringBarTextStyle(parent: UITextStyle) : UITextStyle(parent) {

    override val level: String = "filters"

    public val button: Button = Button(this)

    public class Button(parent: FilteringBarTextStyle) : UITextStyle(parent) {
        override val level: String = "filter"

        public val name: Name = Name(this)

        public class Name(parent: Button) : UITextStyle(parent) {
            override val level: String = "name"
            public val normal: IKFont get() = toIKFont("normal", IKFont.TextStyle.headlineS)
            public val selected: IKFont get() = toIKFont("selected", IKFont.TextStyle.headlineS)
        }
    }
}
