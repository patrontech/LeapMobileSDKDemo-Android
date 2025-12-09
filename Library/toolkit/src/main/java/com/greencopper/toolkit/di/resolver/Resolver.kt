package com.greencopper.toolkit.di.resolver

import com.greencopper.toolkit.di.binding.Params
import com.greencopper.toolkit.di.container.Key
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

public interface Resolver {
    public fun <T : Any> resolve(
        createdType: KClass<T>,
        tag: Any,
        params: Params = Params()
    ): Pair<Key, T?>

    public fun filter(
        predicate: (Key) -> Boolean
    ): List<Key>
}

public inline fun <reified T : Any> Resolver.filterType(
    allowSubclasses: Boolean,
    noinline predicate: (Key) -> Boolean = { true }
): List<Key> {
    val klass = T::class
    val klassPredicate: (Key) -> Boolean = { key ->
        ((allowSubclasses && key.klass.isSubclassOf(klass)) || key.klass == klass) && predicate(key)
    }
    return filter(klassPredicate)
}

public inline fun <reified T : Any> Resolver.filterType(
    noinline predicate: (Key) -> Boolean = { true }
): List<Key> = filterType<T>(false, predicate)

public inline fun <reified T : Any> Resolver.tryResolve(
    vararg args: Any?,
    tag: Any = Unit
): T? = resolve(T::class, tag, Params(*args)).second

public inline fun <reified T : Any> Resolver.resolve(
    vararg args: Any?,
    tag: Any = Unit
): T = resolve(T::class, tag, Params(*args)).getInstance()

public inline fun <reified T : Any> Resolver.resolveAll(
    vararg args: Any?,
    allowSubclasses: Boolean = false,
    tag: Any? = null,
    noinline predicate: (Key) -> Boolean = { true }
): List<T> =
    filterType<T>(allowSubclasses, predicate).mapNotNull { key ->
        if(tag != null && tag != key.tag) {
            null
        } else {
            resolve(key.klass, key.tag, Params(*args)).getInstance() as T
        }
    }

public inline fun <reified T: Any> Resolver.lazyResolver(
    tag: Any = Unit
): LazyResolver<T> = LazyResolver.create(this, tag)

public inline fun <reified T: Any> Resolver.lazy(
    vararg args: Any?,
    tag: Any = Unit
): Lazy<T> = kotlin.lazy { resolve(T::class, tag, Params(*args)).getInstance() }
