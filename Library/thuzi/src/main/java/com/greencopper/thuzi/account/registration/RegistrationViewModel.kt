package com.greencopper.thuzi.account.registration

import androidx.lifecycle.ViewModel
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import com.greencopper.thuzi.account.registration.model.PrepareResult
import com.greencopper.thuzi.account.registration.model.RegistrationData
import com.greencopper.thuzi.account.registration.model.RegistrationLayoutData
import com.greencopper.thuzi.services.attendee.AttendeeService

internal class RegistrationViewModel(
    private val localStorage: LocalStorage,
    private val attendeeService: AttendeeService,
    private val registrationManager: ThuziRegistrationManager,
) : ViewModel() {
    suspend fun prepareForRegistration(
        data: RegistrationLayoutData
    ): PrepareResult = registrationManager.prepareForRegistration(data)

    internal fun saveAndSendRegistrationData(registrationData: RegistrationData) =
        registrationManager.saveAndSendRegistrationData(registrationData)

    internal fun completeRegistration() {
        localStorage.project.thuzi.registered.value = localStorage.project.thuzi.jwt.value != null
        attendeeService.fetchAndDispatch()
    }
}
