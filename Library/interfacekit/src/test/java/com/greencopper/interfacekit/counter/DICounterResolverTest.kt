package com.greencopper.interfacekit.counter

import com.greencopper.testmocks.bindCounter
import com.greencopper.testmocks.interfacekit.MockCounter
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.ResolveException
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class DICounterResolverTest {
    private lateinit var counterResolver: CounterResolver
    private val mockLogger = MockLogging()

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
        counterResolver = DICounterResolver(App, mockLogger)
    }

    @Test
    @DisplayName("Given a valid counter registered, When calling resolve, Then the counter should be returned")
    fun resolveShouldReturnCounter() {
        val params = MockCounter.MockCounterParams().encodeToJsonElement()
        val mockCounter = MockCounter(Counter.Key("Counter1", 1), params)
        bindCounter(mockCounter.key) { mockCounter }
        val counter = counterResolver.resolve(mockCounter.key, params)
        assertThat(counter).isEqualTo(mockCounter)
    }

    @Test
    @DisplayName("Given no counter is registered, When calling resolve, Then null should be returned")
    fun resolveShouldReturnNull() {
        val counter = counterResolver.resolve(Counter.Key("Null", -1), JsonNull)
        assertThat(counter).isNull()
        assertThat(mockLogger.lastThrowable).isInstanceOf(ResolveException::class.java)
    }
}
