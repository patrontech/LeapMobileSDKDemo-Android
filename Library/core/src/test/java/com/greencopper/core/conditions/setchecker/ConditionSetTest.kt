package com.greencopper.core.conditions.setchecker

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionSet
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConditionSetTest {

    @Test
    fun whenSerializing_shouldWork() {
        Toolkit.setupTest()
        val json: Json = App.resolve()
        val jsonString =
            """{"predicate":"registration","conditions":{"registration":{"key":{"name":"Thuzi.Registration","version":1},"params":{"isRegistered":true},"fallback":true}}}"""
        val serialized = json.decodeFromString(ConditionSet.serializer(), jsonString)
        assertThat(serialized.conditions).hasSize(1)
        assertThat(serialized.predicate).isEqualTo("registration")
        val registrationCondition = serialized.conditions["registration"]
            ?: error("Registration condition wasn't found")
        assertThat(registrationCondition.key).isEqualTo(ConditionInfo.Key("Thuzi.Registration", 1))
        val isRegistered = registrationCondition.params!!.jsonObject["isRegistered"]?.jsonPrimitive?.booleanOrNull
        assertThat(isRegistered).isEqualTo(true)
    }
}