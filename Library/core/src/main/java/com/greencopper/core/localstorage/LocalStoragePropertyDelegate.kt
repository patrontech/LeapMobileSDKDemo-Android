package com.greencopper.core.localstorage

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

public typealias MakeAccess<T> = (LocalStorageKey, LocalStorageContainer) -> T
private typealias LocalStoragePropertyCache = MutableMap<LocalStorageKey, LocalStorageProperty<*>>

public class LocalStoragePropertyDelegate<T>(
    private val name: String?,
    private val makeGetter: MakeAccess<Getter<T>>,
    private val makeSetter: MakeAccess<Setter<T>>,
    private val default: T,
    private val transform: (LocalStorage, T, T) -> T = { _, _, value -> value },
) {
    public companion object {
        /**
         * Properties are cached per container, because each property holds a reference to
         * its container in the getter and setter closures that are passed to it.
         *
         * In an application, there will only be one container, so this only affects tests.
         */
        private val propertyCache: MutableMap<LocalStorageContainer, LocalStoragePropertyCache> =
            ConcurrentHashMap()
    }

    @Suppress("UNCHECKED_CAST")
    public operator fun getValue(
        thisRef: LocalStorageDomain,
        property: KProperty<*>,
    ): LocalStorageProperty<T> {
        val key = thisRef.localStorageDomainKey / LocalStorageName(name ?: property.name)
        val container = thisRef.localStorageContainer
        return propertyCache.computeIfAbsent(container) {
            ConcurrentHashMap()
        }.computeIfAbsent(key) {
            val getter = makeGetter(key, container)
            val setter = makeSetter(key, container)
            if (container.getJSON(key) == null) {
                /*
                What is this madness?

                If there's no JSON stored for this value, we write
                its default into the container. We know that getter()
                returns the default if no JSON is present in the container
                for the given key, so we don't need an explicit reference
                to the default.
                */
                setter(getter())
            }
            LocalStorageProperty(key, getter, setter) {
                /*
                 Passing an instance of LocalStorage ensures that the instance
                 being used points to the same project and container as
                 the property being transformed.

                 If the key points to "@" (i.e., the app domain), we can't
                 use it as a project, so we just use "project", which is harmless.
                 */
                val project = if (key.root == "@") {
                    "project"
                } else {
                    key.root
                }
                val localStorage = LocalStorage(project, container)
                transform(localStorage, default, it)
            }
        } as LocalStorageProperty<T>
    }
}

public inline fun <reified T> localStorageProperty(
    default: T,
    name: String? = null,
    noinline transform: (LocalStorage, T, T) -> T = { _, _, value -> value },
): LocalStoragePropertyDelegate<T> {
    val makeGetter: MakeAccess<Getter<T>> = { key, container ->
        { container.get(key, default) }
    }
    val makeSetter: MakeAccess<Setter<T>> = { key, container ->
        { value -> container.set(key, value) }
    }
    return LocalStoragePropertyDelegate(name, makeGetter, makeSetter, default, transform)
}
