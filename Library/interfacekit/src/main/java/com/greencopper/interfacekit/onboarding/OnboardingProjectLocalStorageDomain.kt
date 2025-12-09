package com.greencopper.interfacekit.onboarding

import com.greencopper.core.localstorage.*
import com.greencopper.interfacekit.common.InterfaceKitProjectLocalStorageDomain

internal class OnboardingProjectLocalStorageDomain(parent: InterfaceKitProjectLocalStorageDomain) :
    LocalStorageDomainBase("onboarding", parent) {

    val completedPages: LocalStorageProperty<Set<String>> by localStorageProperty(emptySet())
    val lastOnboardingPageCompletions: LocalStorageProperty<Map<String, Long>>
            by localStorageProperty(emptyMap())
}

internal val InterfaceKitProjectLocalStorageDomain.onboarding: OnboardingProjectLocalStorageDomain
    get() = OnboardingProjectLocalStorageDomain(this)
