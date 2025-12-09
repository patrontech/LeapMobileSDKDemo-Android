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

internal class NoInterestConditionTest {

    init {
        Toolkit.setupTest()
    }

    private val localStorage: LocalStorage = App.resolve()
    private val interestIdsStorage get() = localStorage.project.interfaceKit.interestIds

    private val condition = NoInterestCondition(LazyResolver.adhoc(localStorage))

    @Test
    fun givenEmptyInterestIds_checkWith_returnsTrue() {
        condition.check() shouldBe true
    }

    @Test
    fun givenEmptyInterestIds_checkFlow_returnsTrue() {
        runTest {
            condition.checkFlow().first() shouldBe true
        }
    }

    @Test
    fun givenInterestIds_checkWith_returnsFalse() {
        interestIdsStorage.value = setOf("1", "2", "3")
        condition.check() shouldBe false
    }

    @Test
    fun givenInterestIds_checkFlow_returnsFalse() {
        interestIdsStorage.value = setOf("1", "2", "3")
        runTest {
            condition.checkFlow().first() shouldBe false
        }
    }
}
