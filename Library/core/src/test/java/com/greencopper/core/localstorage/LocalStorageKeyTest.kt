package com.greencopper.core.localstorage

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class LocalStorageKeyTest {
    @BeforeEach
    fun setUp() {
        Toolkit.setupTest()
    }

    @Test
    fun testKeyCorrectness() {
        assertDoesNotThrow {
            val key = LocalStorageKey("@/installationId")
            val localStorage = LocalStorage("tests")
            assertThat(localStorage.app.installationId.key).isEqualTo(key)
            assertThat(localStorage.app.installationId.key.toString()).isEqualTo("@/installationId")
        }
    }

    @Test
    fun testKeyIncorrectness() {
        assertThrows<IllegalArgumentException> {
            LocalStorageKey("&/~abc/@")
        }
    }

    @Test
    fun testKeyConcatenation() {
        val localStorage = LocalStorage("tests")
        val name = "watusi"
        val localStorageName = LocalStorageName(name)
        val key = localStorage.project.thuzi.localStorageDomainKey
        val targetKey = LocalStorageKey("tests/thuzi/watusi")
        assertThat(key / name).isEqualTo(targetKey)
        assertThat(key / localStorageName).isEqualTo(targetKey)
    }
}