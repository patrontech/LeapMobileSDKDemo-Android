package com.greencopper.toolkit.di.binding

/**
 * Class used to pass params.
 */
public class Params(private vararg val params: Any?) {

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(index: Int): T = params[index] as T

    public fun <T> getOrNull(index: Int): T? =
        if (index + 1 <= params.size)
            this[index]
        else null

    public fun toArray(): Array<out Any?> = params
}