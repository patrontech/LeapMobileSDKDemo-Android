package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import kotlinx.coroutines.flow.*

public interface Conditioned {
    public val conditionSet: ConditionSet?
        get() = null
}

public fun <T : Conditioned> T.authorized(conditionChecker: ConditionChecker): Boolean =
    this.conditionSet?.let { conditionChecker.check(it) } ?: true

public fun <T : Conditioned> Collection<T>.authorized(conditionChecker: ConditionChecker): List<T> {
    return this.filter { conditioned -> conditioned.authorized(conditionChecker) }
}

public fun Conditioned.authorizedFlow(conditionChecker: ConditionChecker): Flow<Boolean> =
    conditionSet?.let { conditionChecker.checkFlow(it) } ?: flowOf(true)

public inline fun <reified T : Conditioned> List<T>.authorizedFlow(conditionChecker: ConditionChecker): Flow<List<T>> {
    val checkFlows =
        map { conditioned -> conditioned.authorizedFlow(conditionChecker).map { if (it) conditioned else null } }
    return if (checkFlows.isNotEmpty())
        combine(checkFlows) {
            it.toList().filterNotNull()
        }
    else
        flowOf(emptyList())
}
