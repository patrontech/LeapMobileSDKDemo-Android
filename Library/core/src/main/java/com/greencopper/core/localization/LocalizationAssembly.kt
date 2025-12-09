package com.greencopper.core.localization

import com.greencopper.core.localization.recipe.LocalizationRecipe
import com.greencopper.core.localization.recipe.LocalizationRecipeOverride
import com.greencopper.core.localization.service.JsonLocalizationService
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.translation.LocalStorageTranslationRepository
import com.greencopper.core.localization.translation.TranslationRepository
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*

internal class LocalizationAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton<TranslationRepository>(auto(::LocalStorageTranslationRepository))
            bindProvider<LocalizationService>(auto(::JsonLocalizationService))

            bindRecipe(auto(::LocalizationRecipe))
            bindRecipeOverride(auto(::LocalizationRecipeOverride))
        }
    }
}