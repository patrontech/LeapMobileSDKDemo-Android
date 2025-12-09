package com.greencopper.core.localstorage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LocalStorageTest {
    @Test
    fun whenWritingValueToProperty_shouldReadBackSameValue() {
        val localStorage = LocalStorage("tests")
        val jwt = "abc123"
        localStorage.project.thuzi.jwt.value = jwt
        assertThat(localStorage.project.thuzi.jwt.value).isEqualTo(jwt)
    }

    @Test
    fun whenInterpolatingLocalStorageProperty_itProducesCorrectValue() {
        val localStorage = LocalStorage("tests")
        val jwt = "abc123"
        localStorage.project.thuzi.jwt.value = jwt
        assertThat("${localStorage.project.thuzi.jwt}").isEqualTo(jwt)
    }
}
