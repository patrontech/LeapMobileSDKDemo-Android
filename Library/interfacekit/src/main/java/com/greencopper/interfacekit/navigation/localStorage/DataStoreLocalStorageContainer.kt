package com.greencopper.interfacekit.navigation.localStorage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.greencopper.core.localstorage.LocalStorageContainer
import com.greencopper.core.localstorage.LocalStorageKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class DataStoreLocalStorageContainer(private val dataStore: DataStore<Preferences>) : LocalStorageContainer {
    override fun getJSON(key: LocalStorageKey): String? {
        return runBlocking {
            dataStore.data.first()[stringPreferencesKey(key.toString())]?.toString()
        }
    }

    override fun setJSON(key: LocalStorageKey, json: String) {
        runBlocking {
            dataStore.edit { pref ->
                if (json == "null") {
                    pref.minusAssign(stringPreferencesKey(key.toString()))
                } else {
                    pref[stringPreferencesKey(key.toString())] = json
                }
            }
        }
    }
}
