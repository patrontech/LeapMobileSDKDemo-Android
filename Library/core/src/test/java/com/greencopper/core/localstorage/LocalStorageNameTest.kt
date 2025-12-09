package com.greencopper.core.localstorage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class LocalStorageNameTest {
    @Test
    fun testNameCorrectness() {
        val name = "watusi"
        assertDoesNotThrow {
            LocalStorageName(name)
        }
    }

    @Test
    fun testNameIncorrectness() {
        assertThrows<IllegalArgumentException> {
            LocalStorageName("&%--")
        }
    }

    @Test
    fun testNameEquality() {
        val name = "xyz"
        val localStorageName1 = LocalStorageName(name)
        val localStorageName2 = LocalStorageName(name)
        assertThat(localStorageName1).isEqualTo(localStorageName2)
    }

    @Test
    fun testNameHashCode() {
        val names: MutableMap<LocalStorageName, Int> = mutableMapOf()
        val name = LocalStorageName("int")
        names[name] = 1969
        assertThat(names[name]).isEqualTo(1969)
    }
}