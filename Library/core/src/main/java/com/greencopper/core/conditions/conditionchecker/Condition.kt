package com.greencopper.core.conditions.conditionchecker

import com.greencopper.core.conditions.ConditionParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException

public interface Condition {
    @Throws(IllegalArgumentException::class, SerializationException::class)
    public fun check(params: ConditionParameters?): Boolean
    @Throws(IllegalArgumentException::class, SerializationException::class)
    public fun checkFlow(params: ConditionParameters?): Flow<Boolean>
}
