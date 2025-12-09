package com.greencopper.thuzi.account.registration.manager

import android.webkit.CookieManager
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.core.remotestate.dispatch
import com.greencopper.interfacekit.rootview.RootLayoutManager
import com.greencopper.thuzi.account.DeviceSessionManager
import com.greencopper.thuzi.localstorage.ThuziState
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.model.RegistrationData
import com.greencopper.thuzi.account.registration.model.RegistrationLayoutData
import com.greencopper.thuzi.account.registration.plugins.CustomLogoutAction
import com.greencopper.thuzi.account.registration.plugins.RegistrationPreparer
import com.greencopper.thuzi.account.registration.plugins.RegistrationProcessor
import kotlinx.serialization.json.JsonNull

internal class ConcreteThuziRegistrationManager(
    private val localStorage: LocalStorage,
    private val rootLayoutManager: RootLayoutManager,
    private val registrationPreparer: RegistrationPreparer,
    private val registrationProcessor: RegistrationProcessor,
    private val deviceSessionManager: DeviceSessionManager,
    private val customLogoutAction: CustomLogoutAction?,
    private val contentManager: ContentManager,
    private val remoteState: RemoteStateDispatcher,
) : ThuziRegistrationManager {

    override suspend fun prepareForRegistration(data: RegistrationLayoutData) =
        registrationPreparer.prepareForRegistration(data)

    override fun saveAndSendRegistrationData(registration: RegistrationData) =
        registrationProcessor.processRegistration(registration)

    override fun logout() {
        logout(localStorage.project.localStorageDomainName.toString())
        for (project in contentManager.previousProjects) {
            logout(project)
        }
    }

    private fun logout(project: String) {
        val projectLocalStorage = localStorage[project]
        projectLocalStorage.project.thuzi.apply {
            deviceSessionManager.logout(project)
            jwtExpirationDate.value = null
            jwt.value = null
            registered.value = false
            attendeeId.value = null
            qrCode.value = null
            userFirstName.value = null

            badges.value = emptyList()
            state.value = ThuziState()

            CookieManager.getInstance().removeAllCookies(null)

            remoteState.dispatch(
                "thuzi",
                JsonNull,
                RemoteStateEntry.Domain.PROJECT,
                true,
                project,
            )
        }

        customLogoutAction?.onLogout()
    }

    override suspend fun deleteAccount() {
        logout()
        rootLayoutManager.updateRootLayout()
    }
}
