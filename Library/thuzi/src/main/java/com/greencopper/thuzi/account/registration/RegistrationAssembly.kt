package com.greencopper.thuzi.account.registration

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.commands.system.bindCommand
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.thuzi.account.registration.commands.DeviceLinkingCommand
import com.greencopper.thuzi.account.registration.copyroutine.ConcreteCopyRoutine
import com.greencopper.thuzi.account.registration.copyroutine.CopyRoutine
import com.greencopper.thuzi.account.registration.initializer.RegistrationInitializer
import com.greencopper.thuzi.account.registration.manager.ConcreteThuziRegistrationManager
import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import com.greencopper.thuzi.account.registration.manager.logout.JwtExpirationChecker
import com.greencopper.thuzi.account.registration.manager.logout.ThuziLogoutManager
import com.greencopper.thuzi.account.registration.plugins.CustomLogoutAction
import com.greencopper.thuzi.account.registration.plugins.DefaultRegistrationPreparer
import com.greencopper.thuzi.account.registration.plugins.DefaultRegistrationProcessor
import com.greencopper.thuzi.account.registration.plugins.NoOpLogoutAction
import com.greencopper.thuzi.account.registration.plugins.RegistrationPreparer
import com.greencopper.thuzi.account.registration.plugins.RegistrationProcessor
import com.greencopper.thuzi.account.registration.recipe.RegistrationConfigurationHolder
import com.greencopper.thuzi.account.registration.recipe.RegistrationRecipe
import com.greencopper.thuzi.account.registration.recipe.RegistrationRecipeOverride
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class RegistrationAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton { RegistrationConfigurationHolder() }
            bindRecipe(auto(::RegistrationRecipe))
            bindRecipeOverride(auto(::RegistrationRecipeOverride))
            bindProvider<ThuziRegistrationManager>(auto(::ConcreteThuziRegistrationManager))

            bindFeature(RegistrationInitializer.key, auto(::RegistrationInitializer))
            bindProvider<OnboardingPageInitializer>(
                tag = OnboardingPageKey(
                    RegistrationInitializer.key.name,
                    RegistrationInitializer.key.version
                ),
                auto(::RegistrationInitializer)
            )

            bindViewModel(auto(::RegistrationViewModel))

            bindCondition(ThuziRegisteredCondition.key, auto(::ThuziRegisteredCondition))

            bindCommand(DeviceLinkingCommand.key) {
                DeviceLinkingCommand(
                    resolve(),
                    CoroutineScope(Dispatchers.IO)
                )
            }

            bindProvider<CopyRoutine>(auto(::ConcreteCopyRoutine))
            bindProvider<RegistrationPreparer>(auto(::DefaultRegistrationPreparer))
            bindProvider<RegistrationProcessor>(auto(::DefaultRegistrationProcessor))
            bindProvider<CustomLogoutAction>(auto(::NoOpLogoutAction))

            bindProvider(auto(::JwtExpirationChecker))
            bindSingleton(auto(::ThuziLogoutManager))
        }
    }
}
