package com.greencopper.interfacekit.permissions

import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.localStorageProperty
import com.greencopper.interfacekit.common.InterfaceKitAppLocalStorageDomain

internal class PermissionsAppLocalStorageDomain(parent: InterfaceKitAppLocalStorageDomain) :
    LocalStorageDomainBase("permissions", parent) {

    val askedPermissions: LocalStorageProperty<Set<String>> by localStorageProperty(emptySet())
}

internal val InterfaceKitAppLocalStorageDomain.permissions: PermissionsAppLocalStorageDomain
    get() = PermissionsAppLocalStorageDomain(this)