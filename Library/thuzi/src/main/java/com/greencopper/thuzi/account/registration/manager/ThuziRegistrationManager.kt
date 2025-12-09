package com.greencopper.thuzi.account.registration.manager

import com.greencopper.thuzi.account.registration.model.PrepareResult
import com.greencopper.thuzi.account.registration.model.RegistrationData
import com.greencopper.thuzi.account.registration.model.RegistrationLayoutData

public interface ThuziRegistrationManager {
    public suspend fun prepareForRegistration(data: RegistrationLayoutData): PrepareResult

    public fun saveAndSendRegistrationData(registration: RegistrationData)

    public fun logout()

    public suspend fun deleteAccount()
}
