package com.greencopper.core.conditions.conditionchecker

import com.greencopper.core.conditions.ConditionParameters
import kotlinx.coroutines.flow.Flow

public abstract class UnparameterizedCondition : Condition {
    public abstract fun check(): Boolean
    override fun check(params: ConditionParameters?): Boolean = check()
    public abstract fun checkFlow(): Flow<Boolean>
    override fun checkFlow(params: ConditionParameters?): Flow<Boolean> = checkFlow()
}