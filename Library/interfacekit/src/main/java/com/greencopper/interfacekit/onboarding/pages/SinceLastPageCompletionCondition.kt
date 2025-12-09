package com.greencopper.interfacekit.onboarding.pages

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.onboarding.onboarding
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.time.Instant

internal class SinceLastPageCompletionCondition(private val lazyLocalStorage: LazyResolver<LocalStorage>)
    : ParameterizedCondition<SinceLastPageCompletionCondition.SinceLastPageCompletionConditionData>() {

    internal companion object {
        internal val key: ConditionInfo.Key =
            ConditionInfo.Key("InterfaceKit.Onboarding.SinceLastPageCompletion", 1)
    }

    override fun checkWith(parameter: SinceLastPageCompletionConditionData): Boolean =
        check(getPageCompletions().value, parameter)

    override fun checkWithFlow(parameter: SinceLastPageCompletionConditionData): Flow<Boolean> =
        getPageCompletions().state.map { pages -> check(pages, parameter) }

    override fun deserialize(conditionParameters: ConditionParameters): SinceLastPageCompletionConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    private fun getPageCompletions() =
        lazyLocalStorage.resolve().project.interfaceKit.onboarding.lastOnboardingPageCompletions


    private fun check(pages: Map<String, Long>, parameter: SinceLastPageCompletionConditionData): Boolean {
        val lastCompletionSeconds = pages[parameter.pageId] ?: return true
        return Instant.now().epochSecond - lastCompletionSeconds > parameter.atLeastSince
    }

    @Serializable
    internal data class SinceLastPageCompletionConditionData(
        val pageId: String,
        val atLeastSince: Long,
    ) : KiboSerializable<SinceLastPageCompletionConditionData> {
        override fun getSerializer() = serializer()
    }
}
