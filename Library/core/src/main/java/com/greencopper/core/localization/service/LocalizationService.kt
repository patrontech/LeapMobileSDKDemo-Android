package com.greencopper.core.localization.service

public interface LocalizationService {

    /** Returns the [String] associated with this [key] or null if no associated value was found */
    public fun getStringFromRepository(key: String): String?

    /** Returns the [String] associated with this [key] for the default locale specified by the CMS
     * This should mostly be used for Analytics. */
    public fun getDefaultLocaleString(key: String): String

    /** Returns the plural [String] associated with this [key] and [quantity] or returns null if no associated value was found */
    public fun getQuantityStringFromRepository(key: String, quantity: Int): String?
}

/** Returns the [String] associated with this [key] or [default] if no associated value was found */
public fun LocalizationService.getStringOrDefault(key: String, default: String): String =
    getStringFromRepository(key) ?: default

/** Returns the [String] associated with this [key] or the [key] itself if no associated value was found */
public fun LocalizationService.getString(key: String): String =
    getStringFromRepository(key) ?: key

/** Returns the [String] associated with this [key]
 * or the [key] itself if no associated value was found
 * or null if the key was null */
@JvmName("getStringOrNull")
public fun LocalizationService.getString(key: String?): String? =
    key?.let { getStringFromRepository(key) ?: key }

/** Retrieve the plural [String] associated with this [key] and [quantity] or returns [default] if no associated value was found */
public fun LocalizationService.getQuantityStringOrDefault(key: String, quantity: Int, default: String): String =
    getQuantityStringFromRepository(key, quantity) ?: default

/** Returns the plural [String] associated with this [key] and [quantity] or returns [key] if no associated value was found */
public fun LocalizationService.getQuantityString(key: String, quantity: Int): String =
    getQuantityStringFromRepository(key, quantity) ?: key


