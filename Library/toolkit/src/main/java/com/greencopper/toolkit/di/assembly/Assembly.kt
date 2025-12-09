package com.greencopper.toolkit.di.assembly

import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.Resolver

public interface Assembly {
    public fun registerBindings(registrar: Registrar)

    /**
     * Use this method to perform actions after all
     * bindings have been registered, such as instantiating
     * services, etc.
     *
     * This is called by `Toolkit.setup` after _all_
     * assemblies have registered their bindings.
     *
     * The default implementation does nothing.
     */
    public fun onBindingsRegistered(resolver: Resolver) {
        // Default implementation does nothing
    }
}