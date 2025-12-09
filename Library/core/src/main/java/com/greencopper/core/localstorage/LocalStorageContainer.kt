package com.greencopper.core.localstorage

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorageJsonFactory.Companion.LOCAL_STORAGE_TAG
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public interface LocalStorageContainer {
    /**
     * Reads some JSON (as a String) from the container.
     *
     * Don't call this method! It's here to support the `get`
     * method but cannot be private due to inlining restrictions.
     */
    public fun getJSON(key: LocalStorageKey): String?
    /**
     * Writes some JSON (as a String) to the container.
     *
     * Don't call this method! It's here to support the `set`
     * method but cannot be private due to inlining restrictions.
     */
    public fun setJSON(key: LocalStorageKey, json: String)

    public fun keyExists(key: LocalStorageKey): Boolean =
        getJSON(key) != null
}

public inline fun <reified T> LocalStorageContainer.get(key: LocalStorageKey, default: T): T =
    getJSON(key)?.let {
        KiboSerializable.decodeFromString<T>(it)
    } ?: default

public inline fun <reified T> LocalStorageContainer.set(key: LocalStorageKey, value: T): Unit =
    setJSON(key, App.resolve<Json>(tag = LOCAL_STORAGE_TAG).encodeToString(value))
