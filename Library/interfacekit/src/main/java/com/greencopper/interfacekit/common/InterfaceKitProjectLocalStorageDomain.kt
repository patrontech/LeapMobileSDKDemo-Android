package com.greencopper.interfacekit.common

import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.ProjectLocalStorageDomain
import com.greencopper.core.localstorage.localStorageProperty

public class InterfaceKitProjectLocalStorageDomain(parent: ProjectLocalStorageDomain) :
    LocalStorageDomainBase("interfaceKit", parent) {

    public val interestIds: LocalStorageProperty<Set<String>> by localStorageProperty(emptySet())
}

public val ProjectLocalStorageDomain.interfaceKit: InterfaceKitProjectLocalStorageDomain
    get() = InterfaceKitProjectLocalStorageDomain(this)
