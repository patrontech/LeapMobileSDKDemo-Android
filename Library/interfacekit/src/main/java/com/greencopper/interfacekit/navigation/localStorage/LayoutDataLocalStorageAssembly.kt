package com.greencopper.interfacekit.navigation.localStorage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.greencopper.core.CoreAssembly
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localstorage.*
import com.greencopper.core.localstorage.LocalStorageJsonFactory.Companion.LOCAL_STORAGE_TAG
import com.greencopper.interfacekit.navigation.layout.ConcreteLayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.*

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
internal const val LAYOUT_DATA_LOCAL_STORAGE_CONTAINER_DI_TAG: String = "LayoutDataLocalStorageContainer"
public const val LAYOUT_DATA_LOCAL_STORAGE_DI_TAG: String = "LayoutDataLocalStorage"

internal class LayoutDataLocalStorageAssembly : Assembly {

    @Suppress("NAME_SHADOWING")
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<LayoutDataProvider> {
                ConcreteLayoutDataProvider(
                    resolve(tag = LAYOUT_DATA_LOCAL_STORAGE_DI_TAG),
                    resolve(tag = CoreAssembly.singleThreadScopeTag)
                )
            }

            bindSingleton(tag = LAYOUT_DATA_LOCAL_STORAGE_CONTAINER_DI_TAG) {
                val context: Context? = tryResolve()
                context?.let {
                    ComputedPropertiesLocalStorageContainer(
                        DataStoreLocalStorageContainer(
                            context.dataStore
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
            bindProvider(tag = LAYOUT_DATA_LOCAL_STORAGE_DI_TAG) {
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
                LocalStorage(project, resolve(tag = LAYOUT_DATA_LOCAL_STORAGE_CONTAINER_DI_TAG))
            }
        }
    }
}
