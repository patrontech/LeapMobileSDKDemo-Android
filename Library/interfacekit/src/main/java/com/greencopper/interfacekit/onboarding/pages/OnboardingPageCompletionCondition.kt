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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class OnboardingPageCompletionCondition(private val lazyLocalStorage: LazyResolver<LocalStorage>) :
    ParameterizedCondition<OnboardingPageCompletionCondition.OnboardingPageCompletionConditionData>() {

    override fun checkWith(parameter: OnboardingPageCompletionConditionData): Boolean =
        check(getCompletedPages().value, parameter)

    override fun checkWithFlow(parameter: OnboardingPageCompletionConditionData): Flow<Boolean> =
        getCompletedPages().state.map { pages -> check(pages, parameter) }

    override fun deserialize(conditionParameters: ConditionParameters): OnboardingPageCompletionConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    private fun getCompletedPages() =
        lazyLocalStorage.resolve().project.interfaceKit.onboarding.completedPages

    private fun check(pages: Set<String>, parameter: OnboardingPageCompletionConditionData): Boolean =
        pages.contains(parameter.pageId) == parameter.completed

    @Serializable
    internal data class OnboardingPageCompletionConditionData(
        val pageId: String,
        val completed: Boolean
    ) :
        KiboSerializable<OnboardingPageCompletionConditionData> {
        override fun getSerializer(): KSerializer<OnboardingPageCompletionConditionData> =
            serializer()
    }

    internal companion object {
        internal val key: ConditionInfo.Key =
            ConditionInfo.Key("InterfaceKit.Onboarding.PageCompletion", 1)
    }

}