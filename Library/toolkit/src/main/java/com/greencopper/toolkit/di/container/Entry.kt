package com.greencopper.toolkit.di.container

import com.greencopper.toolkit.di.binding.Creator
import com.greencopper.toolkit.di.binding.Params
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.logging.LogLevel
import com.greencopper.toolkit.logging.Logging

internal class Entry<T>(
    internal val lifetime: Lifetime,
    private val creator: Creator<T>
) {
    private sealed interface Registration<T>
    private class Singleton<T>(val instance: T): Registration<T>
    private class Provider<T>: Registration<T>

    private var registration: Registration<T> = Provider()

    @Suppress("NAME_SHADOWING")
    internal fun resolve(key: Key, resolver: Resolver, params: Params, logger: Logging? = null): T =
        when (val current = registration) {
            is Singleton<T> -> current.instance
            is Provider<T> -> when (lifetime) {
                Lifetime.PROVIDER -> resolver.creator(params)
                Lifetime.SINGLETON -> synchronized(this) {
                    // While we were waiting on the lock, another
                    // thread might have resolved this singleton already.
                    // This is why we check again.
                    when (val current = registration) {
                        is Singleton<T> -> current.instance
                        is Provider<T> -> {
                            logger?.log(
                                LogLevel.DEBUG,
                                "Resolve singleton $key for the first time.",
                                Container.DI
                            )
                            val instance = resolver.creator(params)
                            registration = Singleton(instance)
                            instance
                        }
                    }
                }
            }
        }

    internal fun reset() {
        if (lifetime == Lifetime.SINGLETON) {
            registration = Provider()
        }
    }
}