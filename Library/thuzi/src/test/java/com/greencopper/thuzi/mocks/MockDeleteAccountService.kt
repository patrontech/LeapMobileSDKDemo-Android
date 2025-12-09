package com.greencopper.thuzi.mocks

import com.greencopper.thuzi.account.deletion.DeleteAccountResult
import com.greencopper.thuzi.account.deletion.DeleteAccountService

internal class MockDeleteAccountService(
    var mockResult: DeleteAccountResult = DeleteAccountResult.SUCCESS,
) : DeleteAccountService {
    var lastDeleteUrl: String? = null
    override suspend fun deleteAccount(apiUrl: String): DeleteAccountResult {
        lastDeleteUrl = apiUrl
        return mockResult
    }
}
