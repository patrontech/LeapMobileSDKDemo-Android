package com.greencopper.interfacekit.color.repository

import com.greencopper.interfacekit.color.ColorsConfiguration
import com.greencopper.interfacekit.color.DefaultColors
import com.greencopper.interfacekit.color.OverrideStatusBar

public interface ColorRepository {
    public fun loadColors(configuration: ColorsConfiguration)
    public fun getOverrideColorInt(levels: List<String>): Int?
    public fun getDefaultColors(): DefaultColors
    public fun getOverrideStatusBarColor(levels: List<String>): OverrideStatusBar?
}
