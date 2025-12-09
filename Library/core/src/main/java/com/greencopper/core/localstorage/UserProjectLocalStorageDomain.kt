package com.greencopper.core.localstorage

public enum class Email(public val key: String) {
    THUZI("thuzi"),
    TICKETMASTER("ticketmaster"),
    SHOWCLIX("showclix")
}

public class UserProjectLocalStorageDomain(
    parent: ProjectLocalStorageDomain
): LocalStorageDomainBase("user", parent) {
    public val email: LocalStorageProperty<Map<String, String?>> by localStorageProperty(mapOf())

    public fun putEmail(type: Email, value: String?) {
        email.value = email.value + Pair(type.key, value)
    }
}

public val ProjectLocalStorageDomain.user: UserProjectLocalStorageDomain
    get() = UserProjectLocalStorageDomain(this)
