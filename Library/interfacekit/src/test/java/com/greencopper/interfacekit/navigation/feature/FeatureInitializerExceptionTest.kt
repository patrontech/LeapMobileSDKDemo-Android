package com.greencopper.interfacekit.navigation.feature

import kotlinx.serialization.json.JsonNull
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test


internal class FeatureInitializerExceptionTest {
    @Test
    fun initParametersNotValid_withDefaultConstructor_shouldNotThrow() {
        assertDoesNotThrow { FeatureInitializerException.ParametersNotValid() }
    }

    @Test
    fun initParametersNotValid_withParamsNull_shouldNotThrow() {
        assertDoesNotThrow { FeatureInitializerException.ParametersNotValid(null) }
    }

    @Test
    fun initParametersNotValid_withParams_shouldNotThrow() {
        val params = JsonNull
        assertDoesNotThrow { FeatureInitializerException.ParametersNotValid(params) }
    }

    @Test
    fun getMessageParametersNotValid_withParams_shouldNotThrow() {
        val params = JsonNull
        assertDoesNotThrow { FeatureInitializerException.ParametersNotValid(params).message }
    }

    @Test
    fun initParametersDecodeFailed_withDefaultConstructor_shouldNotThrow() {
        assertDoesNotThrow { FeatureInitializerException.ParametersDecodeFailed() }
    }

    @Test
    fun initParametersDecodeFailed_withParamsNull_shouldNotThrow() {
        assertDoesNotThrow { FeatureInitializerException.ParametersDecodeFailed(null) }
    }

    @Test
    fun initParametersDecodeFailed_withParams_shouldNotThrow() {
        val params = JsonNull
        assertDoesNotThrow { FeatureInitializerException.ParametersDecodeFailed(params) }
    }

    @Test
    fun getMessageParametersDecodeFailed_withParams_shouldNotThrow() {
        val params = JsonNull
        assertDoesNotThrow { FeatureInitializerException.ParametersDecodeFailed(params).message }
    }
}