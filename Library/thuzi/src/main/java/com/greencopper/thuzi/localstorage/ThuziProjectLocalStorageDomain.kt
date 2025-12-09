package com.greencopper.thuzi.localstorage

import com.greencopper.core.localstorage.*
import com.greencopper.thuzi.badges.data.Badge
import com.greencopper.thuzi.models.DeviceSession
import com.greencopper.thuzi.account.registration.model.RegistrationConfiguration
import java.time.ZonedDateTime

public class ThuziProjectLocalStorageDomain(
    parent: ProjectLocalStorageDomain
): LocalStorageDomainBase("thuzi", parent) {

    private companion object {
        fun <T> checkJWT(localStorage: LocalStorage, default: T, value: T): T {
            return localStorage.project.thuzi.jwtExpirationDate.value?.let {
                val date = ZonedDateTime.parse(it)
                // If the JWT has expired, return null no matter what's in LS
                if (date < ZonedDateTime.now()) default else value
            } ?: default
        }
    }

    public val jwt: LocalStorageProperty<String?> by localStorageProperty(null, transform = ::checkJWT)
    public val jwtExpirationDate: LocalStorageProperty<String?> by localStorageProperty(null)
    public val attendeeId: LocalStorageProperty<String?> by localStorageProperty(null, transform = ::checkJWT)
    public val qrCode: LocalStorageProperty<String?> by localStorageProperty(null, transform = ::checkJWT)
    public val registered: LocalStorageProperty<Boolean> by localStorageProperty(false, transform = ::checkJWT)
    public val state: LocalStorageProperty<ThuziState> by localStorageProperty(ThuziState(), transform = ::checkJWT)

    public val userFirstName: LocalStorageProperty<String?> by localStorageProperty(null, transform = ::checkJWT)
    public val badges: LocalStorageProperty<List<Badge>> by localStorageProperty(emptyList(), transform = ::checkJWT)

    public val config: LocalStorageProperty<RegistrationConfiguration?> by localStorageProperty(null)

    /** This value is managed through DeviceSessionManager and should only be accessed through that class.  */
    public val deviceSession: LocalStorageProperty<DeviceSession?> by localStorageProperty(null)
}

public val ProjectLocalStorageDomain.thuzi: ThuziProjectLocalStorageDomain
    get() = ThuziProjectLocalStorageDomain(this)
