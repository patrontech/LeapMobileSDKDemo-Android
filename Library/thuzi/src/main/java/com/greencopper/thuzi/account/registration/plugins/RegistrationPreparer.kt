package com.greencopper.thuzi.account.registration.plugins

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.copyroutine.CopyRoutine
import com.greencopper.thuzi.account.registration.model.PrepareResult
import com.greencopper.thuzi.account.registration.model.RegistrationLayoutData
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e

public interface RegistrationPreparer {
    public suspend fun prepareForRegistration(data: RegistrationLayoutData): PrepareResult
}

internal class DefaultRegistrationPreparer(
    private val copyRoutine: CopyRoutine,
    private val localStorage: LocalStorage,
): RegistrationPreparer {
    override suspend fun prepareForRegistration(data: RegistrationLayoutData): PrepareResult {
        return localStorage.project.thuzi.jwt.value?.let { currentJwt ->
            PrepareResult(
                data.url,
                bakeCookie(currentJwt),
            )
        } ?: try {
            copyRoutine.getNewJwt(
                data.apiUrl,
                data.brandId,
                data.eventId,
            )?.let { newJwt ->
                PrepareResult(
                    data.activationUrl,
                    bakeCookie(newJwt),
                )
            }
        } catch (throwable: Throwable) {
            App.log.e(
                "Error while fetching new JWT",
                throwable = throwable
            )
            null
        } ?: PrepareResult(
            data.url
        )
    }

    private fun bakeCookie(jwt: String) =
        "mobile=${jwt}; " +
                "path=/; " +
                "Secure;"
}
