package com.greencopper.interfacekit.inbox.localstorage

import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.localStorageProperty
import com.greencopper.interfacekit.common.InterfaceKitProjectLocalStorageDomain
import com.greencopper.interfacekit.inbox.Notifications

internal class InboxProjectLocalStorageDomain(parent: InterfaceKitProjectLocalStorageDomain) :
    LocalStorageDomainBase("inbox", parent) {

    val offlineItems: LocalStorageProperty<Set<Notifications.Notification>> by localStorageProperty(
        emptySet()
    )
}

internal val InterfaceKitProjectLocalStorageDomain.inbox: InboxProjectLocalStorageDomain
    get() = InboxProjectLocalStorageDomain(this)
