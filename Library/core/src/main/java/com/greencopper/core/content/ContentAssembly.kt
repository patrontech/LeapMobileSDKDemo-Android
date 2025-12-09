package com.greencopper.core.content

import com.greencopper.core.content.archive.ConcreteContentArchiveOpener
import com.greencopper.core.content.archive.ContentArchiveOpener
import com.greencopper.core.content.initialcontent.*
import com.greencopper.core.content.manager.*
import com.greencopper.core.content.ota.ConcreteOTAManager
import com.greencopper.core.content.ota.OTAManager
import com.greencopper.core.content.ota.repository.OTARepository
import com.greencopper.core.content.ota.repository.RemoteOTARepository
import com.greencopper.core.content.projectswitcher.ConcreteProjectSwitcher
import com.greencopper.core.content.projectswitcher.ProjectSwitcher
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.resolve

internal class ContentAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {

        registrar.run {

            bindProvider<ContentArchiveOpener>(auto(::ConcreteContentArchiveOpener))

            bindSingleton<ContentManager> {
                ConcreteContentManager(
                    processor = resolve(),
                    contentSelector = resolve(),
                    contentHistory = resolve(),
                    coreConfigurationHolder = resolve(),
                    projectCleaner = resolve(),
                    logging = resolve(),
                )
            }

            bindSingleton {
                resolve<ContentManager>() as CurrentProjectTagProvider
            }

            bindSingleton<ContentInitializer> {
                ConcreteContentInitializer(
                    manager = resolve(),
                    storageManager = resolve(),
                    runConfigContent = resolve<RunConfiguration>().content,
                    logging = resolve(),
                )
            }

            bindProvider<ContentProcessor> {
                ConcreteContentProcessor(
                    archiveOpener = resolve(),
                    storageManager = resolve(),
                    contentConfig = resolve<RunConfiguration>().content
                )
            }

            bindSingleton {
                RunConfiguration.build(
                    storageManager = resolve(),
                    json = resolve(),
                )
            }

            bindProvider<OTARepository>(auto(::RemoteOTARepository))

            bindProvider<OTAManager> {
                ConcreteOTAManager(
                    repository = resolve(),
                    contentManager = resolve(),
                    draftContentManager = resolve(),
                    contentConfig = resolve<RunConfiguration>().content,
                    otaApiUrl = resolve<CoreConfigurationHolder>().currentConfiguration.value?.ota?.apiUrl
                        ?: throw IllegalStateException("A content should be applied before instantiating ConcreteOTAManager"),
                    logging = resolve(),
                )
            }

            bindProvider<ContentSelector> {
                ConcreteContentSelector(resolve<RunConfiguration>().content.schema, resolve())
            }

            bindProvider<ProjectSwitcher> {
                ConcreteProjectSwitcher(resolve(), resolve())
            }

            bindProvider<ContentHistory> { ConcreteContentHistory(resolve(), resolve()) }

            bindProvider<ProjectCleaner> { ConcreteProjectCleaner(resolve()) }
        }
    }
}
