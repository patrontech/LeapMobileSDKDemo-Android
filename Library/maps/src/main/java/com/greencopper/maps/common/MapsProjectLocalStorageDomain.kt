package com.greencopper.maps.common

import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.ProjectLocalStorageDomain
import com.greencopper.core.localstorage.localStorageProperty

internal class MapsProjectLocalStorageDomain(parent: ProjectLocalStorageDomain) :
    LocalStorageDomainBase("maps", parent) {
    val myLocations: LocalStorageProperty<Set<String>> by localStorageProperty(emptySet())
}

internal val ProjectLocalStorageDomain.maps: MapsProjectLocalStorageDomain
    get() = MapsProjectLocalStorageDomain(this)
