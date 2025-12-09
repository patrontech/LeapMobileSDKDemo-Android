package com.greencopper.thuzi.services.attendee

import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.content.manager.waitForContentApply
import com.greencopper.core.localstorage.Email
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.ProjectLocalStorageDomain
import com.greencopper.core.localstorage.user
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.core.remotestate.dispatch
import com.greencopper.thuzi.ThuziAPI
import com.greencopper.thuzi.localstorage.Attendee
import com.greencopper.thuzi.localstorage.ThuziState
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.models.DispatchedThuziState
import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import com.greencopper.thuzi.account.registration.model.RegistrationConfiguration
import com.greencopper.thuzi.account.registration.recipe.RegistrationConfigurationHolder
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import retrofit2.HttpException
import java.net.HttpURLConnection

internal class ConcreteAttendeeService(
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val registrationManager: ThuziRegistrationManager,
    private val remoteStateDispatcher: RemoteStateDispatcher,
    private val thuziAPI: ThuziAPI,
    private val registrationConfigurationHolder: RegistrationConfigurationHolder,
    private val currentProjectTagProvider: CurrentProjectTagProvider,
    private val scope: CoroutineScope,
    private val contentManager: ContentManager,
    private val logging: Logging,
) : AttendeeService {

    init {
        scope.launch {
            registrationConfigurationHolder.currentConfiguration
                .waitForContentApply(currentProjectTagProvider)
                .collectLatest {
                    fetchAndDispatch()
                }
        }
    }

    private val registrationConfig: RegistrationConfiguration?
        get() = registrationConfigurationHolder.currentConfiguration.value

    private val projectStorage: ProjectLocalStorageDomain?
        get() = registrationConfig?.project?.let { lazyLocalStorage.resolve()[it].project }

    private suspend fun fetchAndDispatchAsync() {
        // early return if we're missing required fields
        val config = registrationConfig ?: return
        val projectStorage = this@ConcreteAttendeeService.projectStorage ?: return
        val jwt = projectStorage.thuzi.jwt.value ?: return
        val qrCode = projectStorage.thuzi.qrCode.value ?: return

        try {
            val attendeeUrl =
                "${config.apiUrl}/brand/${config.brandId}/event/${config.eventId}/attendee/tokens/${qrCode}"
            val profileUrl =
                "${config.apiUrl}/brand/${config.brandId}/event/${config.eventId}/attendee/token/${qrCode}/profile"

            val attendeeResponse = thuziAPI.getAttendee(attendeeUrl, "Bearer $jwt")
            val profileResponse = thuziAPI.getProfile(profileUrl, "Bearer $jwt").toMutableMap()

            attendeeResponse.customAnswers.forEachIndexed { index, answer ->
                answer?.let {
                    if (answer.isNotBlank()) {
                        profileResponse["$index"] = answer
                    }
                }
            }
            projectStorage.user.putEmail(Email.THUZI, attendeeResponse.email)

            val attendee = Attendee(
                postalCode = attendeeResponse.postalCode?.takeIf { it.isNotBlank() },
                firstName = attendeeResponse.firstName,
                lastName = attendeeResponse.lastName,
                email = attendeeResponse.email,
            )
            applyAndDispatch(
                ThuziState(profileResponse, attendee, attendeeResponse.virtualAccessCards.map { card -> card.id })
            )
        } catch (t: Throwable) {
            logging.e("Thuzi request failed", throwable = t)

            when ((t as? HttpException)?.code()) {
                HttpURLConnection.HTTP_FORBIDDEN, HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    registrationManager.logout()
                }
            }
        }
    }

    private fun applyAndDispatch(thuziState: ThuziState) {
        val dispatchedThuziState = DispatchedThuziState(
            registered = true,
            answers = thuziState.answers,
            postalCode = thuziState.attendee.postalCode,
            virtualAccessCards = thuziState.virtualAccessCards ?: listOf(),
        )
        projectStorage?.thuzi?.state?.value = thuziState
        remoteStateDispatcher.dispatch(
            DispatchedThuziState.dispatcherKey,
            dispatchedThuziState,
            RemoteStateEntry.Domain.PROJECT,
            true
        )
    }

    override fun fetchAndDispatch() {
        scope.launch {
            delay(1000)
            this@ConcreteAttendeeService.fetchAndDispatchAsync()
        }
    }
}

@Serializable
internal data class VirtualAccessCard(
    val id: String,
)
