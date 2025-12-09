package com.greencopper.interfacekit.links

import com.greencopper.interfacekit.links.recipe.LinksRecipe
import com.greencopper.interfacekit.links.recipe.LinksRecipeOverride
import com.greencopper.interfacekit.links.resolver.ConcreteLinkResolver
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*

internal class LinksAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindRecipe(auto(::LinksRecipe))
            bindRecipeOverride(auto(::LinksRecipeOverride))
            bindSingleton { LinksConfigurationHolder() }
            bindProvider<LinkResolver>(auto(::ConcreteLinkResolver))
        }
    }
}