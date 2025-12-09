package com.greencopper.thuzi.account.registration.manager.logout

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.rootview.RootLayoutManager
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * This class exists because we don't want to test the [ThuziLogoutManager]
 * directly. This class consists of pure business logic.
 */
internal class JwtExpirationChecker(
    private val thuziRegistrationManager: ThuziRegistrationManager,
    private val localStorage: LocalStorage,
    private val rootLayoutManager: RootLayoutManager,
) {
    /**
     * Checks whether the JWT has expired and if so, logs out.
     *
     * You will notice that I don't check whether the JWT expiration
     * date is in the past. That's because it's irrelevant. Due
     * to the way LS works, if we have an expiration date but the JWT
     * is null, it's because the JWT expired but the user did not
     * log out manually.
     *
     * So if we (1) have an expiration date but (2) don't have a JWT,
     * then we want to log out. Logging out will null out the expiration
     * date and so this won't be called again.
     *
     * The main purpose of this is to update the root layout so
     * that the conditions run again. That happens in the [ThuziLogoutManager].
     */
    suspend fun checkExpiration() {
        val thuzi = localStorage.project.thuzi
        if (thuzi.jwtExpirationDate.value != null && thuzi.jwt.value == null) {
            thuziRegistrationManager.logout()
            rootLayoutManager.updateRootLayout()
        }
    }
}