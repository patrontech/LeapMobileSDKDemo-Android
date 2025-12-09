package com.greencopper.core.conditions

import com.greencopper.core.localstorage.CoreAppLocalStorageDomain
import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.localStorageProperty

internal class ConditionTestAppLocalStorageDomain(
    parent: CoreAppLocalStorageDomain
): LocalStorageDomainBase("conditionTest", parent) {
    internal val test: LocalStorageProperty<Boolean>
        by localStorageProperty(false)
}

internal val CoreAppLocalStorageDomain.conditionTest: ConditionTestAppLocalStorageDomain
    get() = ConditionTestAppLocalStorageDomain(this)