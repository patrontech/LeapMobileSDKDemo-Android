package com.greencopper.toolkit.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LazyVarTest {

    @Test
    fun test_get() {
        var lazyVar: String by lazyVar { "test1" }
        assertThat(lazyVar).isEqualTo("test1")

        lazyVar = "test2"
        assertThat(lazyVar).isEqualTo("test2")
    }

    @Test
    fun test_set() {
        var lazyVar: String by lazyVar { "test1" }
        lazyVar = "test2"
        assertThat(lazyVar).isEqualTo("test2")
    }
}
