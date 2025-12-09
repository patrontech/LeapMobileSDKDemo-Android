package com.greencopper.toolkit.di.binding

import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.container.Key
import com.greencopper.toolkit.di.container.Lifetime
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

public interface Registrar {
    public fun <T: Any> bind(
        createdType: KClass<T>,
        lifetime: Lifetime,
        tag: Any,
        creator: Creator<T>
    ): Key

    public fun bindAssembly(vararg assemblies: Assembly)

    public fun unbind(key: Key): Boolean

    public fun <T: Any> reset(createdType: KClass<T>, tag: Any)
}

public inline fun <reified T: Any> Registrar.bindSingleton(
    tag: Any,
    noinline creator: Creator<T>
): Key = bind(T::class, Lifetime.SINGLETON, tag, creator)

public inline fun <reified T: Any> Registrar.bindSingleton(
    noinline creator: Creator<T>
): Key = bindSingleton(Unit, creator)

public inline fun <reified T: Any, reified I: T> Registrar.bindSingleton(
    tag: Any = Unit
): Key = bindSingleton<T>(tag) { I::class.createInstance() }

public inline fun <reified T: Any> Registrar.bindProvider(
    tag: Any,
    noinline creator: Creator<T>
): Key = bind(T::class, Lifetime.PROVIDER, tag, creator)

public inline fun <reified T: Any> Registrar.bindProvider(
    noinline creator: Creator<T>
): Key = bindProvider(Unit, creator)

public inline fun <reified T: Any> Registrar.bindRecipe(
    noinline creator: Creator<T>
): Key = bindProvider(tag = "recipe", creator)

public inline fun <reified T: Any> Registrar.bindRecipeOverride(
    noinline creator: Creator<T>
): Key = bindProvider(tag = "recipeOverride", creator)

public inline fun <reified T: Any, reified I: T> Registrar.bindProvider(
    tag: Any = Unit
): Key = bindProvider<T>(tag) { I::class.createInstance() }

public inline fun <reified T: Any> Registrar.unbind(
    tag: Any = Unit
): Boolean = unbind(Key.create<T>(tag))