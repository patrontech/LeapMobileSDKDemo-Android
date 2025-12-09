package com.greencopper.thuzi.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class CustomAnswerCondition(
    private val lazyLocalStorage: LazyResolver<LocalStorage>
) : ParameterizedCondition<CustomAnswerCondition.CustomAnswerConditionData>() {

    @Serializable
    internal enum class Method {
        @SerialName("equals")
        EQUALS {
            override fun check(answer: String, caseInsensitive: Boolean, pattern: String): Boolean =
                answer.equals(pattern, caseInsensitive)
        },
        @SerialName("contains")
        CONTAINS {
            override fun check(answer: String, caseInsensitive: Boolean, pattern: String): Boolean =
                answer.contains(pattern, caseInsensitive)
        };

        internal abstract fun check(
            answer: String,
            caseInsensitive: Boolean,
            pattern: String
        ): Boolean
    }

    internal companion object {
        internal val key = ConditionInfo.Key("Thuzi.CustomAnswer", 1)
    }

    @Serializable
    internal data class CustomAnswerConditionData(
        val answer: String,
        val method: Method,
        val negated: Boolean,
        val caseInsensitive: Boolean,
        val pattern: String
    ) : KiboSerializable<CustomAnswerConditionData> {
        override fun getSerializer(): KSerializer<CustomAnswerConditionData> = serializer()
    }

    override fun checkWith(parameter: CustomAnswerConditionData): Boolean =
        lazyLocalStorage.resolve().project.thuzi.state.value.answers[parameter.answer]?.let { answer ->
            parameter.method.check(answer, parameter.caseInsensitive, parameter.pattern) ==
                    !parameter.negated
        } ?: parameter.negated

    override fun checkWithFlow(parameter: CustomAnswerConditionData): Flow<Boolean> =
        lazyLocalStorage.resolve().project.thuzi.state.state.map { checkWith(parameter) }

    override fun deserialize(conditionParameters: ConditionParameters): CustomAnswerConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)
}
