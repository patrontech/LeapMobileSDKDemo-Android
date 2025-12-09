package com.greencopper.core.localstorage

import android.content.SharedPreferences
import androidx.core.content.edit

public class SharedPreferencesLocalStorageContainer(
    private val prefs: SharedPreferences
): LocalStorageContainer {
    public override fun getJSON(key: LocalStorageKey): String? =
        prefs.getString(key.toString(), null)

    public override fun setJSON(key: LocalStorageKey, json: String): Unit {
        prefs.edit {
            if (json == "null") {
                remove(key.toString())
            } else {
                putString(key.toString(), json)
            }
        }
    }

    public override fun keyExists(key: LocalStorageKey): Boolean =
        prefs.contains(key.toString())
}