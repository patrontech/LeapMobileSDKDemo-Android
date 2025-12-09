package com.greencopper.toolkit.testing

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UnimplementedTest {
    @Test
    fun callWithMessage_shouldThrowWithMessage() {
        assertThrows<NotImplementedError>(message = "this is an error message") {
            unimplemented("this is an error message")
        }
    }

    @Test
    fun callWithoutMessage_shouldThrowWithDefaultMessage() {
        assertThrows<NotImplementedError>(message = "Function not implemented") {
            unimplemented()
        }
    }
}
