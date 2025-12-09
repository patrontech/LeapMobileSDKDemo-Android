package com.greencopper.interfacekit.color

import com.greencopper.interfacekit.color.recipe.ColorRecipe
import com.greencopper.interfacekit.color.recipe.ColorRecipeOverride
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.color.repository.ConcreteColorRepository
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindRecipe
import com.greencopper.toolkit.di.binding.bindRecipeOverride
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.resolve

internal class ColorAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<ColorRepository> {
                ConcreteColorRepository()
            }
            bindRecipe {
                ColorRecipe(
                    colorRepository = resolve()
                )
            }
            bindRecipeOverride {
                ColorRecipeOverride(
                    context = App.resolve(),
                    colorRepository = resolve()
                )
            }
        }
    }
}
