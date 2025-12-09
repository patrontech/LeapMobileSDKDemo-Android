package com.greencopper.interfacekit.textstyle.subsystem

import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*

internal class TextStyleAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindRecipe(auto(::TextStyleRecipe))
            bindRecipeOverride(auto(::TextStyleRecipeOverride))

            bindSingleton<TextStyleRepository>(auto(::ConcreteTextStyleRepository))
        }
    }
}
