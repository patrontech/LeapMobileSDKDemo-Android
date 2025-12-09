package com.greencopper.core.localstorage

public abstract class LocalStorageDomainBase(
    name: String,
    parent: LocalStorageDomain
): LocalStorageDomain {
    public override val localStorageDomainName: LocalStorageName = LocalStorageName(name)
    public override val localStorageDomainParent: LocalStorageDomain? = parent
    public override val localStorageContainer: LocalStorageContainer = parent.localStorageContainer
}