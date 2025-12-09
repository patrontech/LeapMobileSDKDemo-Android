package com.greencopper.core.localstorage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

public typealias Getter<T> = () -> T
public typealias Setter<T> = (value: T) -> Unit

public class LocalStorageProperty<T>(
    public val key: LocalStorageKey,
    private val getter: Getter<T>,
    private val setter: Setter<T>,
    private val transform: (T) -> T = { value -> value },
) {
    private val _state: MutableStateFlow<T> = MutableStateFlow(getter())

    public var value: T
        get() = transform(_state.value)
        set(newValue) {
            setter(newValue)
            _state.value = newValue
        }

    public val state: Flow<T> = _state
        .map { transform(it) }
        .distinctUntilChanged()

    public override fun toString(): String = value.toString()

    /**
     * Writes the default value to the container unless
     * it is already present.
     *
     * Consider the following property:
     *
     * ```
     * public val installationId: LocalStorageProperty<String>
     *   by localStorageProperty(UUID.randomUUID().toString())
     * ```
     *
     * This is the declaration of `installationId` with its
     * default value. When this property is first accessed,
     * the property delegate will write its default value to
     * the underlying container if no existing value is found.
     *
     * However, when doing URL parameter substitution, properties
     * are not used directly. Consider:
     *
     * ```
     * localStorage.replaceURLParameters("?i={@/installationId}")
     * ```
     *
     * The key `@/installationId` is looked up directly in the container.
     * The property delegate is completely bypassed. But this presents
     * a problem. The default for the property may not have been written
     * to disk yet.
     *
     * *Any* use of the property will write the default to disk. This is
     * why `writeDefaultIfNeeded` has no implementation. But ask yourself
     * which of the following examples is clearer:
     *
     * ```
     * init {
     *   // Writes the default to disk if needed
     *   val ignored = localStorage.app.installationId
     * }
     *
     * init {
     *   localStorage.app.installationId.writeDefaultIfNeeded()
     * }
     * ```
     *
     * Both of these do the same thing, but the intent of the second one
     * is much clearer.
     */
    public fun writeDefaultIfNeeded(): Unit = Unit
}
