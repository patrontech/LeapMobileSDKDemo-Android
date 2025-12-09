package com.greencopper.interfacekit.lists.ui

@Deprecated(
    message = "Use the new EmptyViewColors linked to the standalone EmptyView",
    replaceWith = ReplaceWith("com.greencopper.interfacekit.empty.ui.EmptyViewColors")
)
public interface EmptyViewColors {
    public val title: Int
    public val subtitle: Int
}
