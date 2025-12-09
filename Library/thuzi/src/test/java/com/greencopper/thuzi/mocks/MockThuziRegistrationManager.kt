package com.greencopper.thuzi.mocks

import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import com.greencopper.thuzi.account.registration.model.PrepareResult
import com.greencopper.thuzi.account.registration.model.RegistrationData
import com.greencopper.thuzi.account.registration.model.RegistrationLayoutData

internal class MockThuziRegistrationManager(
    var getPrepareForRegistrationResponse: (RegistrationLayoutData) -> PrepareResult = { throw NotImplementedError()}
) : ThuziRegistrationManager {

    var saveRegistrationDataCalled = false
    var logoutCalled = false
    var deleteAccountCalled = false

    override suspend fun prepareForRegistration(data: RegistrationLayoutData): PrepareResult =
        getPrepareForRegistrationResponse(data)

    override fun saveAndSendRegistrationData(registration: RegistrationData) {
        saveRegistrationDataCalled = true
    }

    override fun logout() {
        logoutCalled = true
    }

    override suspend fun deleteAccount() {
        deleteAccountCalled = true
    }
}