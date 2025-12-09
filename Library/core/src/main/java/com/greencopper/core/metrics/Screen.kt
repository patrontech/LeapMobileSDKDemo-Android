package com.greencopper.core.metrics

/** Defines what class a screen belongs to, and what its specific name is.
 *
 * It is recommended to create extensions on the companion as functions
 * taking in a [name] while the [klass] stays static and returning a [Screen] instance.
 */
public data class Screen(val name: String, val klass: String) {
    public companion object
}
