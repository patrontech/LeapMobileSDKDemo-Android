package com.greencopper.thuzi.fanscan

import com.greencopper.core.deferredcommand.*
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.ThuziAPI
import com.greencopper.thuzi.account.DeviceSessionManager
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.toolkit.logging.*
import retrofit2.HttpException
import java.net.HttpURLConnection

internal class FanscanDeferredCommand(
    private val thuziAPI: ThuziAPI,
    private val localStorage: LocalStorage,
    private val deviceSessionManager: DeviceSessionManager,
    private val logger: Logging,
): DeferredCommand {
    internal companion object {
        const val MAXIMUM_ATTEMPTS: Int = 3
    }

    override val key: DeferredCommandKey
        get() = DeferredCommandKey.fanscan

    override suspend fun execute(state: DeferredCommandState): Set<DeferredCommandState> =
        try {
            state.get<FanscanCheckIn>()?.let { checkIn ->
                val localStorage = localStorage[checkIn.project]
                localStorage.project.thuzi.jwt.value?.let { jwt ->
                    val deviceSession = deviceSessionManager.getDeviceSession(checkIn.project).urn
                    execute(checkIn, jwt, deviceSession)
                } ?: setOf(state) // Wait until we have a JWT
            } ?: emptySet() // If there's no state, we bail.
        } catch (e: Throwable) {
            logger.e(
                "An exception occurred when attempting to perform a delayed Fanscan Check-In: ${e.message}",
                throwable = e
            )
            // This is for unrecoverable errors. We discard the
            // state and thus the command.
            emptySet()
        }

    private suspend fun execute(
        checkIn: FanscanCheckIn,
        jwt: String,
        deviceSession: String
    ): Set<DeferredCommandState> =
        try {
            val caller = FanscanApiCaller(jwt, thuziAPI)
            caller.checkIn(checkIn.checkInUrl, checkIn.moduleId, deviceSession)
            // Check-in succeeded. We don't need to run the command again.
            emptySet()
        } catch (e: HttpException) {
            handleStatusCodeException(e, checkIn)
        } catch (_: Exception) {
            // Connectivity error, try again later
            setOf(newState(checkIn))
        }

    private fun handleStatusCodeException(
        e: HttpException,
        checkIn: FanscanCheckIn,
    ): Set<DeferredCommandState> =
        when (e.code()) {
            HttpURLConnection.HTTP_NOT_FOUND -> emptySet()
            HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN -> {
                // Sign out
                val localStorage = localStorage[checkIn.project]
                localStorage.project.thuzi.jwtExpirationDate.value = null
                // Try again later
                setOf(newState(checkIn))
            }
            else -> {
                logger.e(
                    "A connectivity exception occurred when trying to perform a delayed Fanscan Check-In: ${e.message}.",
                    throwable = e
                )
                // Something weird happened
                checkIn.attempts += 1
                if (checkIn.attempts < MAXIMUM_ATTEMPTS) {
                    // Try again later
                    setOf(newState(checkIn))
                } else {
                    logger.i("Maximum retry attempts for Fanscan Check-In with module id ${checkIn.moduleId} was reached. Abandoning further attempts")
                    // We've exhausted our retry attempts
                    emptySet()
                }
            }
        }

    private fun newState(checkIn: FanscanCheckIn): DeferredCommandState =
        DeferredCommandState.create(DeferredCommandKey.fanscan, checkIn)
}

internal val DeferredCommandKey.Companion.fanscan: DeferredCommandKey
    by lazy { DeferredCommandKey("Thuzi.FanScan") }
