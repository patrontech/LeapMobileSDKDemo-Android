package com.greencopper.toolkit.di.resolver

import com.greencopper.toolkit.di.binding.Params
import com.greencopper.toolkit.di.container.Key
import kotlin.reflect.KClass

public class LazyResolver<T: Any>(
    private val klass: KClass<T>,
    private val resolver: Resolver,
    private val tag: Any
) {
    public companion object {
        public inline fun <reified T: Any> create(
            resolver: Resolver,
            tag: Any = Unit
        ): LazyResolver<T> = LazyResolver(T::class, resolver, tag)

        @Suppress("UNCHECKED_CAST")
        public inline fun <reified T: Any> adhoc(instance: T): LazyResolver<T> {
            val key = Key.create<T>()
            val resolver = object: Resolver {
                override fun <T : Any> resolve(
                    createdType: KClass<T>,
                    tag: Any,
                    params: Params
                ): Pair<Key, T?> = Pair(key, instance as T)

                override fun filter(predicate: (Key) -> Boolean): List<Key> =
                    listOf(key)
            }
            return create(resolver)
        }
    }

    public fun resolve(vararg args: Any, tag: Any? = null): T =
        resolver.resolve(klass, tag ?: this.tag, Params(*args)).getInstance()

    public fun tryResolve(vararg args: Any, tag: Any? = null): T? =
        resolver.resolve(klass, tag ?: this.tag, Params(*args)).second
}