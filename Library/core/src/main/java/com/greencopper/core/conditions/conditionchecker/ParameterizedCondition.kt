package com.greencopper.core.conditions.conditionchecker

import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.ParameterizedConditionException
import com.greencopper.core.data.KiboSerializable
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlin.jvm.Throws

public abstract class ParameterizedCondition<T: KiboSerializable<T>> : Condition {

    public abstract fun checkWith(parameter: T): Boolean

    @Throws(ParameterizedConditionException::class)
    override fun check(params: ConditionParameters?): Boolean {
        if (params == null) {
            throw(ParameterizedConditionException.ParamsRequired)
        } else {
            try {
                return checkWith(deserialize(params))
            } catch (serializationException: SerializationException) {
                throw ParameterizedConditionException.ParseErrorException(serializationException.message ?: "While parsing $params something went wrong")
            } catch (classCastException: ClassCastException) {
                throw ParameterizedConditionException.ParseErrorException(classCastException.message ?: "While parsing $params and then casting it, something went wrong")
            }
        }
    }

    public abstract fun checkWithFlow(parameter: T): Flow<Boolean>

    @Throws(ParameterizedConditionException::class)
    override fun checkFlow(params: ConditionParameters?): Flow<Boolean> {
        if (params == null) {
            throw(ParameterizedConditionException.ParamsRequired)
        } else {
            try {
                return checkWithFlow(deserialize(params))
            } catch (serializationException: SerializationException) {
                throw ParameterizedConditionException.ParseErrorException(serializationException.message ?: "While parsing $params something went wrong")
            } catch (classCastException: ClassCastException) {
                throw ParameterizedConditionException.ParseErrorException(classCastException.message ?: "While parsing $params and then casting it, something went wrong")
            }
        }
    }

    @Throws(SerializationException::class, ClassCastException::class)
    public abstract fun deserialize(conditionParameters: ConditionParameters): T
}