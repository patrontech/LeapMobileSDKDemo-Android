package com.greencopper.interfacekit.multiproject

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.multiproject.viewmodel.ProjectSwitcherViewModel
import com.greencopper.interfacekit.multiproject.viewmodel.ProjectSwitchingViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

internal class ProjectSwitcherAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(
                ProjectSwitcherInitializer.key,
                auto(::ProjectSwitcherInitializer)
            )
            bindFeature(
                ProjectSwitchingInitializer.key,
                auto(::ProjectSwitchingInitializer)
            )
            bindViewModel(auto(::ProjectSwitcherViewModel))
            bindViewModel {
                ProjectSwitchingViewModel(
                    resolve(),
                    Executors.newSingleThreadExecutor().asCoroutineDispatcher()
                )
            }

            bindProvider<OnboardingPageInitializer>(
                tag = OnboardingPageKey(
                    ProjectSwitcherInitializer.key.name,
                    ProjectSwitcherInitializer.key.version
                )
            ) {
                ProjectSwitcherInitializer()
            }

            bindProvider<OnboardingPageInitializer>(
                tag = OnboardingPageKey(
                    ProjectSwitchingInitializer.key.name,
                    ProjectSwitchingInitializer.key.version
                )
            ) {
                ProjectSwitchingInitializer()
            }
        }
    }
}
