package com.greencopper.core.localstorage

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LocalStoragePropertyTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    internal fun testEqualityOfPropertyInstancesPerContainer() {
        val container = TestLocalStorageContainer()
        val localStorage1 = LocalStorage("test", container)
        val localStorage2 = LocalStorage("test", container)
        assertThat(localStorage1.app.installationId === localStorage2.app.installationId).isTrue
    }

    @Test
    internal fun testInequalityOfPropertyInstancesForDifferentContainers() {
        val container1 = TestLocalStorageContainer()
        val localStorage1 = LocalStorage("test", container1)
        val container2 = TestLocalStorageContainer()
        val localStorage2 = LocalStorage("test", container2)
        assertThat(localStorage1.app.installationId === localStorage2.app.installationId).isFalse
    }
}
