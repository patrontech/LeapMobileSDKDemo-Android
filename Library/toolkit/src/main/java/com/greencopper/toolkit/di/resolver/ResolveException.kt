package com.greencopper.toolkit.di.resolver

import com.greencopper.toolkit.di.container.Key

public class ResolveException(key: Key): Exception(getMessage(key)) {
    public companion object {
        private fun getMessage(key: Key): String =
            when (key.tag) {
                is Unit -> "Could not resolve type '${key.qualifiedName}'."
                else -> "Could not resolve type '${key.qualifiedName}' with tag '${key.tag}'."
            }
    }
}