package com.greencopper.core.remotestate

import com.greencopper.core.localstorage.CoreProjectLocalStorageDomain
import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.localStorageProperty
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.core.remotestate.models.CustomRemoteState

internal class RemoteStateLocalStorageDomain(
    parent: CoreProjectLocalStorageDomain
): LocalStorageDomainBase("remoteState", parent) {
    internal val messages: LocalStorageProperty<List<RemoteStateEntry>>
        by localStorageProperty(emptyList())
    internal val dispatches: LocalStorageProperty<List<RemoteStateEntry>>
        by localStorageProperty(emptyList())
    internal val custom: LocalStorageProperty<CustomRemoteState>
        by localStorageProperty(CustomRemoteState())
    internal val configuration: LocalStorageProperty<CoreConfiguration.RemoteState?>
        by localStorageProperty(null)
}

internal val CoreProjectLocalStorageDomain.remoteState: RemoteStateLocalStorageDomain
    get() = RemoteStateLocalStorageDomain(this)