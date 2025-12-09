package com.greencopper.core.localstorage

internal class ThuziLocalStorageDomain(parent: LocalStorageDomain):
    LocalStorageDomainBase("thuzi", parent) {
    val jwt: LocalStorageProperty<String?> by localStorageProperty(null)
}

internal val ProjectLocalStorageDomain.thuzi: ThuziLocalStorageDomain
    get() = ThuziLocalStorageDomain(this)