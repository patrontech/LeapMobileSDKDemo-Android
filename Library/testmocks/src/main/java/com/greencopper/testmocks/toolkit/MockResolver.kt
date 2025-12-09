package com.greencopper.testmocks.toolkit

import com.greencopper.toolkit.di.binding.Params
import com.greencopper.toolkit.di.container.Key
import com.greencopper.toolkit.di.resolver.Resolver
import kotlin.reflect.KClass

public class MockResolver(
    public var _resolve: (createdType: KClass<*>, tag: Any, params: Params) -> Pair<Key, Any?> =
        { _, _, _ -> throw NotImplementedError() },
) : Resolver {
    override fun <T : Any> resolve(
        createdType: KClass<T>,
        tag: Any,
        params: Params,
    ): Pair<Key, T?> {
        return _resolve(createdType, tag, params) as Pair<Key, T?>
    }

    override fun filter(predicate: (Key) -> Boolean): List<Key> {
        TODO("Not yet implemented")
    }
}
