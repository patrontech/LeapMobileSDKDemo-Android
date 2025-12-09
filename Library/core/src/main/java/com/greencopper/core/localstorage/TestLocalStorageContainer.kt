package com.greencopper.core.localstorage

/**
 * A `LocalStorageContainer` implementation useful in tests.
 *
 * If you call `App.resolve<LocalStorage>()` in a unit test,
 * the underlying container will be an instance of this type.
 */
public class TestLocalStorageContainer: LocalStorageContainer {
    private val map: MutableMap<LocalStorageKey, String> = mutableMapOf()

    public override fun getJSON(key: LocalStorageKey): String? =
        map[key]

    public override fun setJSON(key: LocalStorageKey, json: String) {
        if (json == "null") {
            map.remove(key)
        } else {
            map[key] = json
        }
    }

    public fun clear() {
        map.clear()
    }
}