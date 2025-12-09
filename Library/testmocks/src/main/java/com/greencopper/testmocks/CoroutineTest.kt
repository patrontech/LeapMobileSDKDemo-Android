package com.greencopper.testmocks

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import kotlin.coroutines.CoroutineContext

public abstract class CoroutineTest(public val dispatcher: CoroutineContext = Dispatchers.Default) {

    public val testScope: CoroutineScope = CoroutineScope(dispatcher)

    init {
        if (dispatcher is TestDispatcher) {
            Dispatchers.setMain(dispatcher)
        }
    }

    @AfterEach
    public fun resetCoroutine() {
        testScope.cancel()
        if (dispatcher is TestDispatcher) {
            Dispatchers.resetMain()
        }

        afterEach()
    }

    public abstract fun afterEach()
}
