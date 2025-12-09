package com.greencopper.toolkit.testing

@Throws
public fun unimplemented(message: String? = null): Nothing {
    throw NotImplementedError(message ?: "Function not implemented")
}
