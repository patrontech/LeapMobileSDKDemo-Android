package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.Condition
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class TestCondition : Condition {

    companion object {
        val key = ConditionInfo.Key("TestCondition", 1)
    }

    override fun check(params: ConditionParameters?): Boolean {
        params ?: throw IllegalArgumentException("Params shouldn't be null")
        val value = params.jsonObject.getOrDefault("value", JsonNull)
        return value.jsonPrimitive.boolean
    }

    override fun checkFlow(params: ConditionParameters?): Flow<Boolean> = flow { emit(check(params)) }
}

internal class TestUpdatingCondition(private val localStorage: LocalStorage) : Condition {

    companion object {
        val key = ConditionInfo.Key("TestUpdatingCondition", 1)
    }

    override fun check(params: ConditionParameters?): Boolean {
        return localStorage.app.core.conditionTest.test.value
    }

    override fun checkFlow(params: ConditionParameters?): Flow<Boolean>
            = localStorage.app.core.conditionTest.test.state
}