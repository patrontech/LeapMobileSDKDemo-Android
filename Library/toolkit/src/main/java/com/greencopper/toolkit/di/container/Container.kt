package com.greencopper.toolkit.di.container

import com.greencopper.toolkit.di.assembly.Assembler
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.logging.LogLevel
import com.greencopper.toolkit.logging.Logging
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import com.greencopper.toolkit.di.assembly.Entry as AssemblyEntry

internal class Container(
    private val logger: Logging? = null,
    private val parent: Container? = null,
): Registrar, Resolver, Assembler {

    internal companion object {
        const val DI = "DI"
    }

    private val lock = ReentrantReadWriteLock()
    private val entries: MutableMap<Key, Entry<*>> = mutableMapOf()
    private val assemblies: LinkedHashSet<AssemblyEntry> = linkedSetOf()

    internal operator fun get(key: Key): Entry<*>? =
        lock.read { entries[key] ?: parent?.get(key) }

    internal operator fun set(key: Key, value: Entry<*>) =
        lock.write { entries[key] = value }

    override fun filter(predicate: (Key) -> Boolean): List<Key> =
        lock.read { entries.keys.filter(predicate) }

    override fun <T: Any> bind(
        createdType: KClass<T>,
        lifetime: Lifetime,
        tag: Any,
        creator: Creator<T>
    ): Key {
        val key = Key(createdType, tag)
        logger?.log(LogLevel.DEBUG, "Bind $key with lifetime $lifetime.", DI)
        this[key] = Entry(lifetime, creator)
        return key
    }

    override fun unbind(key: Key): Boolean {
        return lock.write { entries.remove(key) != null }
    }

    override fun <T : Any> reset(createdType: KClass<T>, tag: Any) {
        this[Key(createdType, tag)]?.reset()
    }

    @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
    override fun <T: Any> resolve(
        createdType: KClass<T>,
        tag: Any,
        params: Params
    ): Pair<Key, T?> {
        val key = Key(createdType, tag)
        return key to when (createdType) {
            Registrar::class -> this as T
            else -> this[key]?.let { entry ->
                val entry = entry as Entry<T>
                logger?.log(
                    LogLevel.DEBUG,
                    "Resolve $key with lifetime ${entry.lifetime}.",
                    DI
                )
                entry.resolve(key, this, params, logger)
            }
        }
    }

    private fun bindOneAssembly(assembly: Assembly) {
        val entry = AssemblyEntry(assembly)
        if (assemblies.contains(entry)) return
        // We call register bindings first,
        // because inside of registerBindings
        // may be calls to bindAssembly for dependent
        // assemblies.
        assembly.registerBindings(this)
        assemblies.add(AssemblyEntry(assembly))
    }

    override fun bindAssembly(vararg assemblies: Assembly) {
        for (assembly in assemblies) {
            bindOneAssembly(assembly)
        }
    }

    override fun assemble(assemblies: List<Assembly>) {
        bindAssembly(*assemblies.toTypedArray())
        this.assemblies.forEach { it.assembly.onBindingsRegistered(this) }
        this.assemblies.clear()
    }
}
