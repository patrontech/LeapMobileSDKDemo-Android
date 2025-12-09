package com.greencopper.thuzi.account.registration.recipe

import com.greencopper.core.content.recipe.ConfigurationHolderRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.Resettable
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import com.greencopper.thuzi.account.registration.model.RegistrationConfiguration

internal open class RegistrationRecipe(
    registrationConfigurationHolder: RegistrationConfigurationHolder
) : ConfigurationHolderRecipe<RegistrationConfiguration, RegistrationConfigurationHolder>(
    registrationConfigurationHolder,
    KiboSerializable.Companion::decodeFromString,
    RegistrationConfiguration::writeToPath
), Resettable {

    override val key: ContentRecipeKey = ContentRecipeKey("Thuzi.Registration", 1, 1)
    override val componentPath: String = "thuzi/registration/"

    override fun reset() {
        configurationHolder.currentConfiguration.value = null
    }
}