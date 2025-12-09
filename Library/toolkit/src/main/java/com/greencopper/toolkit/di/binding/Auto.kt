package com.greencopper.toolkit.di.binding

import com.greencopper.toolkit.di.resolver.resolve

public inline fun <reified T> auto(noinline init: () -> T): Creator<T> = {
    init()
}

/**
 * Higher-order function to assist registration of a dependency with
 * a single transitive dependency that is also registered.
 *
 * This works only with transitive dependencies that are not tagged.
 * If the dependency is tagged, you will have to use a lambda.
 *
 * ```kotlin
 * class SubDependency
 * class Dependency(sub: SubDependency)
 *
 * DI.factory<SubDependency>()
 * DI.factory(auto(::Dependency))
 * ```
 *
 * Be careful not to use this inside of a lambda:
 *
 * ```kotlin
 * // THIS IS WRONG AND WON'T WORK!
 * DI.factory { auto(::Dependency) }
 * ```
 *
 * Instead, remember that this function _creates
 * a lambda_.
 */
public inline fun <reified T, reified D> auto(noinline init: (D) -> T): Creator<T> = {
    init(resolve())
}

/**
 * Higher-order function to assist registration of a dependency with
 * two transitive dependencies that are also registered.
 */
public inline fun <reified T, reified D1, reified D2> auto(
    noinline init: (D1, D2) -> T
): Creator<T> = {
    init(resolve(), resolve())
}

public inline fun <reified T, reified D1, reified D2, reified D3> auto(
    noinline init: (D1, D2, D3) -> T
): Creator<T> = {
    init(resolve(), resolve(), resolve())
}

public inline fun <reified T, reified D1, reified D2, reified D3, reified D4> auto(
    noinline init: (D1, D2, D3, D4) -> T
): Creator<T> = {
    init(resolve(), resolve(), resolve(), resolve())
}

public inline fun <reified T, reified D1, reified D2, reified D3, reified D4, reified D5> auto(
    noinline init: (D1, D2, D3, D4, D5) -> T
): Creator<T> = {
    init(resolve(), resolve(), resolve(), resolve(), resolve())
}

public inline fun <reified T, reified D1, reified D2, reified D3, reified D4, reified D5, reified D6> auto(
    noinline init: (D1, D2, D3, D4, D5, D6) -> T
): Creator<T> = {
    init(resolve(), resolve(), resolve(), resolve(), resolve(), resolve())
}

public inline fun <reified T, reified D1, reified D2, reified D3, reified D4, reified D5, reified D6, reified D7> auto(
    noinline init: (D1, D2, D3, D4, D5, D6, D7) -> T
): Creator<T> = {
    init(resolve(), resolve(), resolve(), resolve(), resolve(), resolve(), resolve())
}

public inline fun <reified T, reified D1, reified D2, reified D3, reified D4, reified D5, reified D6, reified D7, reified D8> auto(
    noinline init: (D1, D2, D3, D4, D5, D6, D7, D8) -> T
): Creator<T> = {
    init(resolve(), resolve(), resolve(), resolve(), resolve(), resolve(), resolve(), resolve())
}
