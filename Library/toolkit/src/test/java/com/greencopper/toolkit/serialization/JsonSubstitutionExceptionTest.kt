package com.greencopper.toolkit.serialization

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JsonSubstitutionExceptionTest {

    @Test
    fun createdExceptionShouldContainsKey() {
        val key = "test"
        assertThat(JsonSubstitutionException.MissingKey(key).message).contains(key)
        assertThat(JsonSubstitutionException.MalformedMatch(key).message).contains(key)
    }
}