package com.greencopper.testmocks.interfacekit

import android.graphics.Color
import com.greencopper.interfacekit.color.*
import com.greencopper.interfacekit.color.repository.ColorRepository

public class MockColorRepository : ColorRepository {
    private val color = Color(Color.BLACK, Color.BLACK)
    private val defaultColors = DefaultColors(
        DefaultColors.StatusBar(
            DefaultColors.StatusBar.Style.LIGHT,
            DefaultColors.StatusBar.Style.LIGHT
        ),
        DefaultColors.Accent(color, color),
        DefaultColors.Background(color, color),
        DefaultColors.Label(color, color, color, color, color, color),
        DefaultColors.Fill(color, color, color, color, color),
        DefaultColors.TopBar(color, color, color),
        DefaultColors.Result(color, color)
    )

    override fun loadColors(configuration: ColorsConfiguration) {}

    override fun getOverrideColorInt(levels: List<String>): Int = Color.BLACK

    override fun getDefaultColors(): DefaultColors = defaultColors

    override fun getOverrideStatusBarColor(levels: List<String>): OverrideStatusBar? = null
}
