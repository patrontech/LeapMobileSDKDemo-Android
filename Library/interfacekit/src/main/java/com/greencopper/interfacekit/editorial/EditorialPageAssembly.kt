package com.greencopper.interfacekit.editorial

import com.greencopper.interfacekit.editorial.recipe.EditorialPageRecipe
import com.greencopper.interfacekit.editorial.recipe.EditorialPageRecipeOverride
import com.greencopper.interfacekit.editorial.repository.ConcreteEditorialPageRepository
import com.greencopper.interfacekit.editorial.repository.EditorialPageRepository
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*

internal class EditorialPageAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<EditorialPageRepository>(auto(::ConcreteEditorialPageRepository))
            bindRecipe(auto(::EditorialPageRecipe))
            bindRecipeOverride(auto(::EditorialPageRecipeOverride))
            bindFeature(EditorialPageInitializer.key, auto(::EditorialPageInitializer))
        }
    }
}
