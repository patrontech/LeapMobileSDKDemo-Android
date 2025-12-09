package com.greencopper.core.conditions.conditionchecker

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionSet
import com.greencopper.core.conditions.parser.ConditionsFlowMap
import com.greencopper.core.conditions.parser.ConditionsMap
import com.greencopper.core.conditions.parser.PredicateParser
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.w
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

internal class ConcreteConditionChecker(
    private val predicateParser: PredicateParser,
    private val conditionResolver: ConditionResolver,
    private val logging: Logging,
) : ConditionChecker {

    override val metadata: MutableStateFlow<JsonElement> = MutableStateFlow(JsonNull)

    override fun check(conditionSet: ConditionSet): Boolean {
        val conditionsMap = conditionSet.conditions.getMap()
        val predicate = predicateParser.parse(conditionSet.predicate)
        return predicate.check(conditionsMap)
    }

    override fun checkFlow(conditionSet: ConditionSet): Flow<Boolean> {
        val conditionsMap = conditionSet.conditions.getFlowMap()
        val predicate = predicateParser.parse(conditionSet.predicate)
        return predicate.checkFlow(conditionsMap)
    }

    override fun check(conditionInfo: ConditionInfo): Boolean {
        val condition = conditionResolver.resolve(conditionInfo.key, metadata) ?: return conditionInfo.fallback
        return try {
            condition.check(conditionInfo.params)
        } catch (t: Throwable) {
            logging.w(message = "Condition checking failed for $conditionInfo", throwable = t)
            conditionInfo.fallback
        }
    }

    override fun checkFlow(conditionInfo: ConditionInfo): Flow<Boolean> {
        val condition = conditionResolver.resolve(conditionInfo.key, metadata) ?: return flowOf(conditionInfo.fallback)
        return condition.checkFlow(conditionInfo.params).catch { throwable ->
            logging.w(message = "Condition checking failed for $conditionInfo", throwable = throwable)
            emit(conditionInfo.fallback)
        }
    }

    private fun Map<String, ConditionInfo>.getFlowMap(): ConditionsFlowMap {
        return this.mapValues { (_, condition) -> checkFlow(condition) }
    }

    private fun Map<String, ConditionInfo>.getMap(): ConditionsMap {
        return this.mapValues { (_, condition) -> { check(condition) } }
    }
}
