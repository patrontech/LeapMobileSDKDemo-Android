package com.greencopper.core.metrics.provider

import com.greencopper.core.services.manager.ServiceAspect

public data class Provider(private val name: String) {
    public companion object

    /**
     * Derives a [ServiceAspect] from a Provider.
     *
     * WARNING: Use with care! For metrics providers,
     * the name of the ServiceAspect is usually the
     * same as the provider, but it does not have to be.
     */
    val serviceAspect: ServiceAspect = ServiceAspect(name)
}

public val Provider.Companion.default: Provider
    by lazy { Provider("default") }
