package com.greencopper.toolkit.extensions

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private class LazyVar<T : Any>(private val initValue: () -> T) : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: initValue().also { setValue(this, property, it) }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

public fun <T: Any> lazyVar(initValue: () -> T): ReadWriteProperty<Any?, T> = LazyVar(initValue)
