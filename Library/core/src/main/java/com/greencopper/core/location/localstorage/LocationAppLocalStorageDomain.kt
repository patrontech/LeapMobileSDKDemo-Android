package com.greencopper.core.location.localstorage

import com.greencopper.core.localstorage.CoreAppLocalStorageDomain
import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.localStorageProperty

internal class LocationAppLocalStorageDomain(
    parent: CoreAppLocalStorageDomain
) : LocalStorageDomainBase("location", parent) {
    internal val currentRegions: LocalStorageProperty<Set<Int>>
            by localStorageProperty(emptySet())
}

internal val CoreAppLocalStorageDomain.location: LocationAppLocalStorageDomain
    get() = LocationAppLocalStorageDomain(this)