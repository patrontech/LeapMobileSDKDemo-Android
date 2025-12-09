package com.greencopper.core.notification.localstorage

import com.greencopper.core.localstorage.CoreAppLocalStorageDomain
import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.localStorageProperty

internal class NotificationAppLocalStorageDomain(
    parent: CoreAppLocalStorageDomain
): LocalStorageDomainBase("notification", parent) {
    internal val isRegistered: LocalStorageProperty<Boolean>
        by localStorageProperty(false)
    internal val firebaseToken: LocalStorageProperty<String?>
        by localStorageProperty(null)
}

internal val CoreAppLocalStorageDomain.notification: NotificationAppLocalStorageDomain
    get() = NotificationAppLocalStorageDomain(this)