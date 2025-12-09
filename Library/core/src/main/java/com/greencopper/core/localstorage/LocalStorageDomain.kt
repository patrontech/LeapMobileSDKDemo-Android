package com.greencopper.core.localstorage

public interface LocalStorageDomain: LocalStorageProvider {
    public val localStorageDomainName: LocalStorageName
    public val localStorageDomainParent: LocalStorageDomain?
}

public val LocalStorageDomain.localStorageDomainKey: LocalStorageKey
    get() = localStorageDomainParent?.let { it.localStorageDomainKey / localStorageDomainName }
        ?: LocalStorageKey(localStorageDomainName)
