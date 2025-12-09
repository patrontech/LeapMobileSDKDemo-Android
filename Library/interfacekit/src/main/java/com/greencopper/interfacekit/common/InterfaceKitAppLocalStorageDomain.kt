package com.greencopper.interfacekit.common

import com.greencopper.core.localstorage.*

internal class InterfaceKitAppLocalStorageDomain(parent: AppLocalStorageDomain) :
    LocalStorageDomainBase("interfaceKit", parent) {
    val layoutData: LocalStorageProperty<Map<Int, String>> by localStorageProperty(emptyMap())
}

internal val AppLocalStorageDomain.interfaceKit: InterfaceKitAppLocalStorageDomain
    get() = InterfaceKitAppLocalStorageDomain(this)
