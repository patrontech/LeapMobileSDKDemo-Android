package com.greencopper.toolkit.extensions

import kotlin.reflect.KProperty1

public inline fun <reified T : Enum<T>, V> KProperty1<T, V>.find(value: V): T =
    findOrNull(value) ?: throw IllegalArgumentException(
        "Could not find enum constant with property `$name` of value `$value`"
    )

public inline fun <reified T : Enum<T>, V> KProperty1<T, V>.findOrDefault(value: V, default: T): T =
    findOrNull(value) ?: default

public inline fun <reified T : Enum<T>, V> KProperty1<T, V>.findOrNull(value: V): T? =
    enumValues<T>().firstOrNull { this(it) == value }
