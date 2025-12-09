package com.greencopper.interfacekit

import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.color.DefaultColors
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.color.toColorInt
import com.greencopper.toolkit.appinstance.AppInstance
import com.greencopper.toolkit.di.resolver.resolve

public fun AppInstance.color(levels: List<String>, defaultColor: Color): Int {
    val colorRepository = resolve<ColorRepository>()
    return colorRepository.getOverrideColorInt(levels) ?: defaultColor.toColorInt()
}

public fun AppInstance.statusBarColor(
    levels: List<String>,
    defaultStatusBarColor: DefaultColors.StatusBar
): DefaultColors.StatusBar {
    val colorRepository = resolve<ColorRepository>()
    val overrideColorStyle =
        colorRepository.getOverrideStatusBarColor(levels) ?: return defaultStatusBarColor
    return DefaultColors.StatusBar(
        overrideColorStyle.light ?: defaultStatusBarColor.light,
        overrideColorStyle.dark ?: defaultStatusBarColor.dark
    )
}
