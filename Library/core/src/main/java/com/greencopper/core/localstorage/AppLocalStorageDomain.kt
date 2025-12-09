package com.greencopper.core.localstorage

import java.util.*

public class AppLocalStorageDomain internal constructor (
    public override val localStorageContainer: LocalStorageContainer
): LocalStorageDomain {
    public override val localStorageDomainName: LocalStorageName = LocalStorageName("@")
    public override val localStorageDomainParent: LocalStorageDomain? = null

    public val installationId: LocalStorageProperty<String>
            by localStorageProperty(UUID.randomUUID().toString())
}