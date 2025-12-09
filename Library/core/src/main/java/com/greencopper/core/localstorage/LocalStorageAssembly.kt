package com.greencopper.core.localstorage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey
import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localstorage.LocalStorageJsonFactory.Companion.LOCAL_STORAGE_TAG
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.*
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

internal class LocalStorageAssembly : Assembly {

    companion object {
        private fun localStorageSharedPreferences(context: Context): SharedPreferences = runBlocking(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("localStoragePrefs", Context.MODE_PRIVATE)

            try {
                val masterKeyBuilder = MasterKey
                    .Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                val masterKey = masterKeyBuilder.build()
                val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "localStorage",
                    masterKey,
                    PrefKeyEncryptionScheme.AES256_SIV,
                    PrefValueEncryptionScheme.AES256_GCM
                )
                if (encryptedSharedPreferences.all.isNotEmpty()) {
                    migrateSharedPrefs(encryptedSharedPreferences, sharedPreferences)
                }
            } catch (e: SecurityException) {
                App.resolve<Logging>().e("Error migrating encrypted shared preferences.", throwable = e)
            }

            return@runBlocking sharedPreferences
        }

        private fun migrateSharedPrefs(encryptedPrefs: SharedPreferences, newPrefs: SharedPreferences) {
            newPrefs.edit {
                encryptedPrefs.all.forEach { key, value ->
                    when (value) {
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Float -> putFloat(key, value)
                        is Boolean -> putBoolean(key, value)
                        is String -> putString(key, value)
                        else -> {
                            if (value is Set<*> && value.all { it is String}) {
                                putStringSet(key, value as Set<String>)
                            } else {
                                throw IllegalStateException("Unsupported type in local storage migration: ${value?.javaClass}")
                            }
                        }
                    }
                }
            }

            encryptedPrefs.edit { clear() }
        }
    }

    @Suppress("NAME_SHADOWING")
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindCondition(LocalStorageQueryCondition.key) {
                LocalStorageQueryCondition(lazyResolver())
            }
            bindSingleton {
                val context: Context? = tryResolve()
                context?.let {
                    ComputedPropertiesLocalStorageContainer(
                        SharedPreferencesLocalStorageContainer(
                            localStorageSharedPreferences(it)
                        ),
                        it,
                        resolve<RunConfiguration>().content,
                        lazyResolver(),
                        lazyResolver(),
                        resolve(),
                        resolve(tag = LOCAL_STORAGE_TAG)
                    )
                } ?: TestLocalStorageContainer()
            }
            bindProvider {
                /*
                 * LocalStorage cannot be a singleton, though its underlying
                 * storage and all instances of LocalStorageProperty are cached.
                 *
                 * The reason it cannot be a singleton is because of multi-project.
                 * Most of LocalStorage is just syntactic sugar for creating hierarchical
                 * keys without using strings.
                 */
                val currentProjectTagProvider = resolve<CurrentProjectTagProvider>()
                val project = currentProjectTagProvider.currentProject
                    ?: resolve<RunConfiguration>().content.project
                LocalStorage(project, resolve())
            }

            bindSingleton(tag = LOCAL_STORAGE_TAG) {
                LocalStorageJsonFactory.create()
            }
        }
    }
}
