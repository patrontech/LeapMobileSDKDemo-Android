package com.greencopper.thuzi.localstorage

import com.greencopper.core.localstorage.AppLocalStorageDomain
import com.greencopper.core.localstorage.LocalStorageDomainBase

internal class ThuziAppLocalStorageDomain(
    parent: AppLocalStorageDomain
): LocalStorageDomainBase("thuzi", parent)

internal val AppLocalStorageDomain.thuzi: ThuziAppLocalStorageDomain
    get() = ThuziAppLocalStorageDomain(this)