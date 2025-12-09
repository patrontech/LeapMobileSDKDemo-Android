package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.textstyle.subsystem.*

public class MockTextStyleRepository : TextStyleRepository {

    override fun loadTextStyles(configuration: TextStyleConfiguration) {
        TODO("Not yet implemented")
    }

    public val iKFontCalls: MutableList<IKFontCallArguments> = mutableListOf()
    override fun getIKFont(levels: List<String>, textStyle: IKFont.TextStyle, vararg fallbacks: IKFont): IKFont {
        iKFontCalls.add(IKFontCallArguments(levels, textStyle, fallbacks.toList()))
        return IKFont(textStyle, listOf(), textStyle.fallbackFont)
    }

    public data class IKFontCallArguments(
        val levels: List<String>,
        val textStyle: IKFont.TextStyle,
        val fallbacks: List<IKFont>,
    )

}
