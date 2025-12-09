package com.greencopper.thuzi

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.model.RegistrationConfiguration
import com.greencopper.thuzi.account.registration.recipe.RegistrationConfigurationHolder
import java.time.ZonedDateTime


internal fun RegistrationConfigurationHolder.setup() {
    currentConfiguration.value = RegistrationConfiguration("", "", "", "", "", "", "", ScreenNameAnalytics(""), "")
}

internal fun RegistrationConfigurationHolder.teardown() {
    currentConfiguration.value = null
}

internal fun setAuthenticated(localStorage: LocalStorage, authenticated: Boolean) {
    localStorage.project.thuzi.jwt.value = if(authenticated) {
        "token"
    } else {
        null
    }

    localStorage.project.thuzi.jwtExpirationDate.value = if(authenticated) {
        ZonedDateTime.now().plusDays(1).toString()
    } else {
        null
    }

    localStorage.project.thuzi.registered.value = authenticated
}
