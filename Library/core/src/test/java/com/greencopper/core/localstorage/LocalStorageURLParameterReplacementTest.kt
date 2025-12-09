package com.greencopper.core.localstorage

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class LocalStorageURLParameterReplacementTest {

    val localStorage: LocalStorage = LocalStorage("params")

    @BeforeEach
    fun setUp() {
        Toolkit.setupTest()
    }

    @Test
    fun whenSubstitutingStringParameterInURL_thenItSucceeds() {
        localStorage.project.thuzi.jwt.value = "abc 123"
        val url = "thuzi?jwt={~/thuzi/jwt =}"
        assertDoesNotThrow {
            val substituted = localStorage.replaceUrlParameters(url)
            assertThat(substituted).isEqualTo("thuzi?jwt=abc+123")
        }
    }

    @Test
    fun whenSubstitutingMissingRequiredParameterInURL_thenItThrowsIllegalArgumentException() {
        assertThrows<IllegalArgumentException> {
            localStorage.replaceUrlParameters("thuzi?jwt={~/thuzi/jwt}")
        }
    }

    @Test
    fun whenSubstitutingEmptyRequiredParameterInURL_thenItThrowsIllegalArgumentException() {
        localStorage.project.thuzi.jwt.value = ""
        assertThrows<IllegalArgumentException> {
            localStorage.replaceUrlParameters("thuzi?jwt={~/thuzi/jwt}")
        }
    }

    @Test
    fun whenSubstitutingMissingOptionalParameterInURL_thenItSucceeds() {
        val url = "thuzi?jwt={~/thuzi/jwt?}"
        assertDoesNotThrow {
            val substituted = localStorage.replaceUrlParameters(url)
            assertThat(substituted).isEqualTo("thuzi?jwt=")
        }
    }

    @Test
    fun whenSubstitutingComplexRequiredParameterInURL_thenItThrowsIllegalArgumentException() {
        val container = localStorage.localStorageContainer
        val key = LocalStorageKey("@/foo/baz")
        // Pretend you never saw this! Don't write to the container directly.
        container.set(key, Irreplaceable(4))
        val url = "patron?x={@/foo/baz}&jwt={~/thuzi/jwt?}"
        assertThrows<IllegalArgumentException> {
            localStorage.replaceUrlParameters(url)
        }
    }

    @Test
    fun whenSubstitutingComplexOptionalParameterInURL_thenItSucceeds() {
        val container = localStorage.localStorageContainer
        val key = LocalStorageKey("@/foo/baz")
        // Pretend you never saw this! Don't write to the container directly.
        container.set(key, Irreplaceable(4))
        val url = "patron?x={@/foo/baz?}"
        assertDoesNotThrow {
            val substituted = localStorage.replaceUrlParameters(url)
            assertThat(substituted).isEqualTo("patron?x=")
        }
    }

    @Test
    fun whenSubstitutingSubscriptParameterInURL_thenItSucceeds() {
        val container = localStorage.localStorageContainer
        val key = LocalStorageKey("@/obj")
        // Pretend you never saw this! Don't write to the container directly.
        container.set(key, mapOf("x" to "foo@bar.com"))
        val url = "patron/{@/obj['x'] /}"
        assertDoesNotThrow {
            val substituted = localStorage.replaceUrlParameters(url)
            assertThat(substituted).isEqualTo("patron/foo%40bar.com")
        }
    }

    @Test
    fun whenSubstitutingEmptyRequiredSubscriptParameterInURL_thenItThrowsIllegalArgumentException() {
        val container = localStorage.localStorageContainer
        val key = LocalStorageKey("@/obj")
        // Pretend you never saw this! Don't write to the container directly.
        container.set(key, mapOf("x" to "foo"))
        val url = "patron?foo={@/obj['z']}" // There is no z
        assertThrows<IllegalArgumentException> {
            localStorage.replaceUrlParameters(url)
        }
    }

    @Test
    fun whenSubstitutingMissingOptionalSubscriptParameterInURL_thenItSucceeds() {
        val container = localStorage.localStorageContainer
        val key = LocalStorageKey("@/obj")
        // Pretend you never saw this! Don't write to the container directly.
        container.set(key, mapOf("x" to "foo"))
        val url = "patron?foo={@/obj['z']?}" // There is no z
        assertDoesNotThrow {
            val substituted = localStorage.replaceUrlParameters(url)
            assertThat(substituted).isEqualTo("patron?foo=")
        }
    }

    @Test
    fun whenSubstitutingRequiredWrongTypeSubscriptParameterInURL_thenItThrowsIllegalArgumentException() {
        val container = localStorage.localStorageContainer
        val key = LocalStorageKey("@/obj")
        // Pretend you never saw this! Don't write to the container directly.
        container.set(key, mapOf("x" to Irreplaceable(99)))
        val url = "patron?foo={@/obj['x']}"
        assertThrows<IllegalArgumentException> {
            localStorage.replaceUrlParameters(url)
        }
    }

    @Test
    fun whenSubstitutingOptionalWrongTypeSubscriptParameterInURL_thenItSucceeds() {
        val container = localStorage.localStorageContainer
        val key = LocalStorageKey("@/obj")
        // Pretend you never saw this! Don't write to the container directly.
        container.set(key, mapOf("x" to Irreplaceable(99)))
        val url = "patron?foo={@/obj['x']?}"
        assertDoesNotThrow {
            val substituted = localStorage.replaceUrlParameters(url)
            assertThat(substituted).isEqualTo("patron?foo=")
        }
    }
}

@Serializable
internal data class Irreplaceable(val i: Int)