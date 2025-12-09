package com.greencopper.thuzi.badges

import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.thuzi.ThuziAssembly.Companion.thuziDirectory
import com.greencopper.thuzi.badges.data.*
import com.greencopper.thuzi.badges.initializer.BadgesInitializer
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File

internal class BadgesAssembly : Assembly {

    companion object {
        internal const val badgesDirectory = "badges"
    }

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<BadgesRepository> {
                ConcreteBadgesRepository(
                    badgesApiService = resolve(),
                    badgesCacheService = resolve(),
                    coroutineContext = Dispatchers.IO,
                    badgesImagesDirectory = resolve(tag = badgesDirectory)
                )
            }

            bindProvider(tag = badgesDirectory) {
                val currentProjectTag = resolve<CurrentProjectTagProvider>().currentProject
                runBlocking {
                    File(
                    resolve<StorageManager>().getProjectFilesStorage(currentProjectTag),
                    "$thuziDirectory/$badgesDirectory"
                    ).path
                }
            }

            bindProvider<BadgesApiService> {
                ConcreteBadgesApiService(
                    thuziAPI = resolve(),
                    localStorage = resolve(),
                    badgesImagesDirectory = resolve(tag = badgesDirectory),
                    backgroundContext = Dispatchers.IO,
                )
            }

            bindProvider<BadgesCacheService> {
                ConcreteBadgesCacheService(
                    localStorage = resolve(),
                    badgesImagesDirectory = resolve(tag = badgesDirectory)
                )
            }

            bindFeature(BadgesInitializer.key) {
                BadgesInitializer()
            }

            bindViewModel(auto(::BadgesViewModel))
        }
    }
}
