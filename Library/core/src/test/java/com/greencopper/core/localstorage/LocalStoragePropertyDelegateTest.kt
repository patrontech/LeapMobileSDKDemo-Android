package com.greencopper.core.localstorage

import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

private val defaultValue = 1
private val transformedValue = 2

internal class LocalStoragePropertyDelegateTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    internal fun testTransform() {
        val localStorage = LocalStorage("test")
        localStorage.project.transform.property.value shouldBe transformedValue
        runTest {
            localStorage.project.transform.property.state.first() shouldBe transformedValue
        }

        localStorage.project.transform.property.value = 3

        localStorage.project.transform.property.value shouldBe transformedValue
        runTest {
            localStorage.project.transform.property.state.first() shouldBe transformedValue
        }
    }

    @Test
    internal fun test() {
        val localStorage = LocalStorage("test")
        println(localStorage.project.transform)
        println(localStorage.project.transform)
        println()
        println(localStorage.project.transform.property === localStorage.project.transform.property)
    }
}

internal class TransformLocalStorageDomain(
    parent: LocalStorageDomain,
) : LocalStorageDomainBase("test", parent) {

    val property: LocalStorageProperty<Int> by localStorageProperty(defaultValue, transform = { _, _, _ -> transformedValue })
}

internal val ProjectLocalStorageDomain.transform: TransformLocalStorageDomain
    get() = TransformLocalStorageDomain(this)
