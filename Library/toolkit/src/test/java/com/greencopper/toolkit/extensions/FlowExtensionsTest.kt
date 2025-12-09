package com.greencopper.toolkit.extensions

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import java.io.IOException

internal class FlowExtensionsTest {

    @Test
    fun whenSafeCollecting_withError_shouldFail() {
        runTest {
            var flowError: Boolean? = null
            flow<Unit> { throw IllegalArgumentException() }.safeCollect(
                onError = { flowError = true },
                onSuccess = { flowError = false }
            )
            assertThat(flowError).isTrue
        }
    }

    @Test
    fun whenSafeCollecting_withoutError_shouldSucceed() {
        runTest {
            var flowError: Boolean? = null
            flow { emit(Unit) }.safeCollect(
                onError = { flowError = true },
                onSuccess = { flowError = false }
            )
            assertThat(flowError).isFalse
        }
    }

    @Test
    fun whenCollectingError_withError_shouldAccess() {
        runTest {
            var errorCollected: Boolean? = null
            flow<Unit> { throw IllegalArgumentException() }.collectError { errorCollected = true }
            assertThat(errorCollected).isTrue
        }
    }

    @Test
    fun whenCollectingError_withoutError_shouldntAccess() {
        runTest {
            var errorCollected: Boolean? = null
            flow { emit(Unit) }.collectError { errorCollected = true }
            assertThat(errorCollected).isNull()
        }
    }

    @Test
    fun whenMappingErrorNonType_withDifferentType_shouldThrowDifferentType() {
        assertThrows<IOException> {
            runTest {
                flow<Unit> { throw IllegalArgumentException() }.mapErrorNotType<Unit, IOException> {
                    throw IOException()
                }.collect {
                    fail { "Collection should throw" }
                }
            }
        }
    }

    @Test
    fun whenMappingErrorNonType_withSameType_shouldThrowSameType() {
        var errorMapped: Boolean? = null
        assertThrows<IllegalArgumentException> {
            runTest {
                flow<Unit> { throw IllegalArgumentException() }.mapErrorNotType<Unit, IllegalArgumentException> {
                    errorMapped = true
                }.collect {
                    fail { "Collection should throw" }
                }
            }
        }
        assertThat(errorMapped).isNull()
    }

    @Test
    fun whenZipMany_withNoValues_shouldEmitEmptyList() {
        val zipped = zipMany<Any>(emptyList())

        runTest {
            zipped.collect { value ->
                assert(value.isEmpty())
                assert(value == emptyList<Any>())
            }
        }
    }

    @Test
    fun whenZipMany_withOneFlow_shouldEmitListOneValue() {
        val flow = flowOf(1)
        val zipped = zipMany(listOf(flow))

        runTest {
            zipped.collect { value ->
                assert(value.size == 1)
                assert(value == listOf(1))
            }
        }
    }

    @Test
    fun whenZipMany_withFiveFlows_shouldEmitListFiveValues() {
        val values = listOf("test", "should", "emit", "five", "values")
        val flow1 = flowOf(values[0])
        val flow2 = flowOf(values[1])
        val flow3 = flowOf(values[2])
        val flow4 = flowOf(values[3])
        val flow5 = flowOf(values[4])
        val zipped = zipMany(listOf(flow1, flow2, flow3, flow4, flow5))

        runTest {
            zipped.collect { flowValue ->
                assert(flowValue.size == values.size)
                assert(flowValue.containsAll(values))
            }
        }
    }

    @Test
    fun whenZipMany_withError_shouldEmitError() {
        val flow1 = flowOf(1)
        val flowError = flow<Any> {
            throw IllegalArgumentException()
        }

        val zipped = zipMany(listOf(flow1, flowError))

        runTest {
            assertThrows<IllegalArgumentException> {
                zipped.collect { value ->
                    print(value)
                }
            }
        }
    }
}