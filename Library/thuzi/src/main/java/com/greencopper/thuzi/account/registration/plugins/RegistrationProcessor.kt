package com.greencopper.thuzi.account.registration.plugins

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.core.remotestate.dispatch
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.models.DispatchedThuziState
import com.greencopper.thuzi.account.registration.model.RegistrationData

public interface RegistrationProcessor {
    public fun processRegistration(registration: RegistrationData)
}

internal class DefaultRegistrationProcessor(
    private val localStorage: LocalStorage,
    private val remoteStateDispatcher: RemoteStateDispatcher,
): RegistrationProcessor {
    override fun processRegistration(registration: RegistrationData) {
        localStorage.project.thuzi.apply {
            jwtExpirationDate.value = registration.authTokenExpiresOn
            jwt.value = registration.authToken
            attendeeId.value = registration.attendeeId
            qrCode.value = registration.qrCode
            userFirstName.value = registration.attendee?.firstName
        }
        sendRegistrationState()
    }

    private fun sendRegistrationState() {
        val thuziState = localStorage.project.thuzi.state.value
        remoteStateDispatcher.dispatch(
            DispatchedThuziState.dispatcherKey,
            DispatchedThuziState(
                true,
                answers = thuziState.answers,
                postalCode = thuziState.attendee.postalCode
            ),
            RemoteStateEntry.Domain.PROJECT,
            false
        )
    }
}
