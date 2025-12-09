package com.greencopper.interfacekit.interests

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class IsAnInterestConditionTest {

    init {
        Toolkit.setupTest()
    }

    private val localStorage: LocalStorage = App.resolve()
    private val interestIdsStorage get() = localStorage.project.interfaceKit.interestIds

    private val condition = IsAnInterestCondition(LazyResolver.adhoc(localStorage))

    @Test
    fun givenEmptyInterestIds_checkWith_returnsFalse() {
        val data = IsAnInterestCondition.InterestData("1")
        condition.checkWith(data) shouldBe false
    }

    @Test
    fun givenEmptyInterestIds_checkWithFlow_returnsFalse() {
        val data = IsAnInterestCondition.InterestData("1")

        runTest {
            condition.checkWithFlow(data).first() shouldBe false
        }
    }

    @Test
    fun givenNoMatchingInterestIds_checkWith_returnsFalse() {
        interestIdsStorage.value = setOf("1", "2", "3")
        val data = IsAnInterestCondition.InterestData("4")

        condition.checkWith(data) shouldBe false
    }

    @Test
    fun givenNoMatchingInterestIds_checkWithFlow_returnsFalse() {
        interestIdsStorage.value = setOf("1", "2", "3")
        val data = IsAnInterestCondition.InterestData("4")

        runTest {
            condition.checkWithFlow(data).first() shouldBe false
        }
    }

    @Test
    fun givenMatchingInterestIds_checkWith_returnsTrue() {
        interestIdsStorage.value = setOf("1", "2", "3")
        val data = IsAnInterestCondition.InterestData("1")

        condition.checkWith(data) shouldBe true
    }

    @Test
    fun givenMatchingInterestIds_checkWithFlow_returnsTrue() {
        interestIdsStorage.value = setOf("1", "2", "3")
        val data = IsAnInterestCondition.InterestData("1")

        runTest {
            condition.checkWithFlow(data).first() shouldBe true
        }
    }

    @Test
    fun deserialize_returnsData() {
        val data = IsAnInterestCondition.InterestData("1")
        val result = condition.deserialize(data.encodeToJsonElement())

        result shouldBe IsAnInterestCondition.InterestData("1")
    }
}
