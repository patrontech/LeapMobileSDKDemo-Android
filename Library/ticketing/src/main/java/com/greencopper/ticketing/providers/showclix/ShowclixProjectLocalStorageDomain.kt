package com.greencopper.ticketing.providers.showclix

import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.ProjectLocalStorageDomain
import com.greencopper.core.localstorage.localStorageProperty
import com.greencopper.ticketing.models.Ticket

internal class ShowclixProjectLocalStorageDomain(
    parent: ProjectLocalStorageDomain
): LocalStorageDomainBase("showclix", parent) {

    val timeToken: LocalStorageProperty<String?> by localStorageProperty(null)
    val validationToken: LocalStorageProperty<String?> by localStorageProperty(null)
    val userId: LocalStorageProperty<String?> by localStorageProperty(null)
    val tickets: LocalStorageProperty<List<Ticket>> by localStorageProperty(emptyList())
}

internal val ProjectLocalStorageDomain.showclix: ShowclixProjectLocalStorageDomain
    get() = ShowclixProjectLocalStorageDomain(this)
