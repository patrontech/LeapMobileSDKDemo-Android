package com.greencopper.interfacekit.counter

import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e

public interface CounterResolver {
    public fun resolve(key: Counter.Key, params: CounterParameters): Counter<*>?
}

internal class DICounterResolver(
    private val resolver: Resolver,
    private val logger: Logging,
) : CounterResolver {
    override fun resolve(key: Counter.Key, params: CounterParameters): Counter<*>? =
        try {
            resolver.resolve(tag = key, args = arrayOf(params))
        } catch (error: Throwable) {
            logger.e("Couldn't resolve Counter $key", throwable = error)
            null
        }
}
