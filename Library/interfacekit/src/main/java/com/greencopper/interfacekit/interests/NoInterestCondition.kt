package com.greencopper.interfacekit.interests

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.conditionchecker.UnparameterizedCondition
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class NoInterestCondition(private val lazyLocalStorage: LazyResolver<LocalStorage>) :
    UnparameterizedCondition() {

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("InterfaceKit.NoInterest", 1)
    }

    private val interestIdsLocalStorage: LocalStorageProperty<Set<String>>
        get() = lazyLocalStorage.resolve().project.interfaceKit.interestIds

    override fun check(): Boolean = interestIdsLocalStorage.value.isEmpty()

    override fun checkFlow(): Flow<Boolean> = interestIdsLocalStorage.state.map { it.isEmpty() }
}
