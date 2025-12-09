package com.greencopper.interfacekit.lists.ui

import com.greencopper.interfacekit.textstyle.subsystem.IKFont

@Deprecated(
    message = "Use the new EmptyViewTextStyles linked to the standalone EmptyView",
    replaceWith = ReplaceWith("com.greencopper.interfacekit.empty.ui.EmptyViewTextStyles")
)
public interface EmptyViewTextStyle {
    public val title: IKFont
    public val subtitle: IKFont
}
