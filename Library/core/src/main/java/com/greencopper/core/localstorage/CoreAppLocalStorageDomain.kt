package com.greencopper.core.localstorage

import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.core.services.iplocation.IPLocation

public class CoreAppLocalStorageDomain(
    parent: AppLocalStorageDomain
): LocalStorageDomainBase("core", parent) {

    public val appRemoteStateQueue: LocalStorageProperty<List<RemoteStateEntry>>
            by localStorageProperty(emptyList())

    public val iplocation: LocalStorageProperty<IPLocation?> by localStorageProperty(null)

    public val draftContentPasscode: LocalStorageProperty<String?> by localStorageProperty(null)
    public val lastUIObservedDraftContentPasscode: LocalStorageProperty<String?> by localStorageProperty(null)
}


public val AppLocalStorageDomain.core: CoreAppLocalStorageDomain
    get() = CoreAppLocalStorageDomain(this)
