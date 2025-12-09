package com.greencopper.core.conditions.parser

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal typealias ConditionsMap = Map<String, () -> Boolean>
internal typealias ConditionsFlowMap = Map<String, Flow<Boolean>>

internal interface Predicate {
    fun check(conditionsMap: ConditionsMap): Boolean
    fun checkFlow(conditionsMap: ConditionsFlowMap): Flow<Boolean>
}

internal enum class Op {
    AND, OR
}

internal data class Id(val id: String): Predicate {
    override fun check(conditionsMap: ConditionsMap): Boolean =
        (conditionsMap[this.id] ?: error("Couldn't find id $id in conditions map")).invoke()

    override fun checkFlow(conditionsMap: ConditionsFlowMap): Flow<Boolean> =
        (conditionsMap[this.id] ?: error("Couldn't find id $id in conditions map"))
}

internal data class Not(val predicate: Predicate): Predicate {
    override fun check(conditionsMap: ConditionsMap): Boolean =
        !predicate.check(conditionsMap)

    override fun checkFlow(conditionsMap: ConditionsFlowMap): Flow<Boolean> =
        predicate.checkFlow(conditionsMap).map { !it }
}

internal data class Logic(
    val left: Predicate,
    val op: Op,
    val right: Predicate
): Predicate {
    override fun check(conditionsMap: ConditionsMap): Boolean =
        when (op) {
            Op.AND -> left.check(conditionsMap) && right.check(conditionsMap)
            Op.OR -> left.check(conditionsMap) || right.check(conditionsMap)
        }

    override fun checkFlow(conditionsMap: ConditionsFlowMap): Flow<Boolean> {
        val flows = listOf(left, right).map { it.checkFlow(conditionsMap) }
        return combine(flows) { booleans ->
            when (op) {
                Op.AND -> booleans.all { it }
                Op.OR -> booleans.any { it }
            }
        }
    }
}

internal class Failing : Predicate {
    override fun check(conditionsMap: ConditionsMap): Boolean = false

    override fun checkFlow(conditionsMap: ConditionsFlowMap): Flow<Boolean> = flowOf(false)
}