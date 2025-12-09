package com.greencopper.thuzi.account.deletion

import com.greencopper.core.localstorage.Email
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.user
import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.secrets.SecretService
import com.greencopper.thuzi.ThuziAPI
import com.greencopper.thuzi.ThuziRequest
import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import java.util.Locale

public enum class DeleteAccountResult {
    SUCCESS,
    FAIL,
}

public interface DeleteAccountService {
    public suspend fun deleteAccount(apiUrl: String): DeleteAccountResult
}

internal class ConcreteDeleteAccountService(
    private val secretService: SecretService,
    private val signatureGenerator: SignatureGenerator,
    private val registrationManager: ThuziRegistrationManager,
    private val thuziAPI: ThuziAPI,
    private val localStorage: LocalStorage,
    private val logger: Logging,
    private val locale: Locale,
) : DeleteAccountService {

    override suspend fun deleteAccount(apiUrl: String): DeleteAccountResult {
        return try {
            val accountDeletionAPiSecret = secretService["accountDeletionApi"] ?: throw SecretNotFoundException()
            val email = localStorage.project.user.email.value[Email.THUZI.key] ?: throw EmailNotFoundException()
            val attendeeId = localStorage.project.thuzi.attendeeId.value ?: throw AttendeeIdNotFoundException()

            val requestBody = ThuziRequest.AccountDeletion(
                locale.toLanguageTag(),
                localStorage.app.installationId.value,
                email,
                attendeeId
            )

            thuziAPI.deleteAccount(
                url = apiUrl,
                authorizationHeader = signatureGenerator.getAuthenticationKey(apiKey = accountDeletionAPiSecret),
                requestBody,
            )

            registrationManager.logout()
            DeleteAccountResult.SUCCESS
        } catch (throwable: Throwable) {
            logger.e("Failed to delete thuzi account", throwable = throwable)
            DeleteAccountResult.FAIL
        }
    }
}

private class SecretNotFoundException : Exception("accountDeletionApi secret not found")
private class EmailNotFoundException : Exception("thuzi email not found")
private class AttendeeIdNotFoundException : Exception("thuzi attendee id not found")
