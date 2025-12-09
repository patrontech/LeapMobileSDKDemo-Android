package com.greencopper.core.deferredcommand

import com.greencopper.core.localstorage.CoreAppLocalStorageDomain
import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.localStorageProperty

internal class DeferredCommandAppLocalStorageDomain(
    parent: CoreAppLocalStorageDomain
): LocalStorageDomainBase("deferredCommand", parent) {
    internal val states: LocalStorageProperty<Set<DeferredCommandState>>
        by localStorageProperty(emptySet())
}

internal val CoreAppLocalStorageDomain.deferredCommand: DeferredCommandAppLocalStorageDomain
    get() = DeferredCommandAppLocalStorageDomain(this)