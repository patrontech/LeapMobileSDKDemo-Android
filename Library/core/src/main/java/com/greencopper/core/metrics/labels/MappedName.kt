package com.greencopper.core.metrics.labels

import com.greencopper.core.metrics.provider.Provider
import com.greencopper.core.metrics.provider.default

public open class MappedName(defaultName: String? = null) {

    private interface Named {
        val title: String?
    }

    private data class Name(val name: String) : Named {
        override val title: String = name
    }

    private object Nameless : Named {
        override val title: String? = null
    }

    private companion object {
        fun title(name: String?): Named {
            return name?.let { Name(name) } ?: Nameless
        }
    }

    private var names: MutableMap<Provider, Named> = mutableMapOf(Provider.default to title(defaultName))

    public operator fun set(providers: Set<Provider>, name: String?) {
        for (provider in providers) {
            names[provider] = title(name)
        }
    }

    public operator fun set(provider: Provider, name: String?) {
        names[provider] = title(name)
    }

    // get the title (name, but it's really confusing using name everywhere)
    // if title is null for that provider, return the default provider title, which should always be there
    public operator fun get(provider: Provider): String? {
        val name = names[provider] ?: names[Provider.default]
        return name?.title
    }

    public fun with(name: String, providers: Set<Provider>): MappedName {
        for (provider in providers) {
            names[provider] = title(name)
        }
        return this
    }

    public fun without(providers: Set<Provider>): MappedName {
        for (provider in providers) {
            names[provider] = title(null)
        }
        return this
    }

    public fun ignore(providers: Set<Provider>) {
        for (provider in providers) {
            names[provider] = Nameless
        }
    }

    public fun reset(): MappedName {
        // Reset the map to only contain the default provider and it's value.
        names = mutableMapOf(Provider.default to names[Provider.default]!!)
        return this
    }
}