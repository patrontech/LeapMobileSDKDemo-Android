package com.greencopper.toolkit.di.container

import kotlin.reflect.KClass

public data class Key(val klass: KClass<*>, val tag: Any) {
    public companion object {
        public inline fun <reified T: Any> create(tag: Any = Unit): Key = Key(T::class, tag)
    }

    val qualifiedName: String
        get() = klass.qualifiedName ?: "<anonymous class>"
}