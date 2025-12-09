package com.greencopper.core.deferredcommand

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DeferredCommandStateTest {
    init {
        Toolkit.setupTest()
    }

    @Test
    fun roundtripSerialization_succeeds() {
        val json: Json = App.resolve()
        val source = DeferredCommandState.create(DeferredCommandKey.test, "foo", json)
        val string = json.encodeToString(source)
        val target: DeferredCommandState = json.decodeFromString(string)

        assertThat(source).isEqualTo(target)
        assertThat(target.get<String>(json)).isEqualTo("foo")
    }
}

private val DeferredCommandKey.Companion.test: DeferredCommandKey
        by lazy { DeferredCommandKey("Test.Test") }
