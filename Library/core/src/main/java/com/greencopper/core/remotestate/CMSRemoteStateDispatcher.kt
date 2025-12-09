package com.greencopper.core.remotestate

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.content.manager.waitForContentApply
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.networking.CoreAPI
import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal class CMSRemoteStateDispatcher(
    private val coreAPI: CoreAPI,
    private val signatureGenerator: SignatureGenerator,
    private val coreConfigurationHolder: CoreConfigurationHolder,
    private val currentProjectTagProvider: CurrentProjectTagProvider,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    override val json: Json,
    private val backgroundScope: CoroutineScope,
) : RemoteStateDispatcher {

    private val dispatchers = HashMap<String, ProjectRemoteStateDispatcher>()
    private val appRemoteStateQueue = lazyLocalStorage.resolve().app.core.appRemoteStateQueue

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    @VisibleForTesting
    internal fun dispatchOnLifecyclePause() {
        backgroundScope.launch {
            dispatchRemoteState()
        }
    }

    private fun dispatchRemoteState() {
        backgroundScope.launch {
            val currentProject = currentProjectTagProvider.currentProject
            val currentProjectDispatcher =
                dispatchers[currentProject] ?: createCurrentProjectRemoteStateDispatcher()
            currentProjectDispatcher.dispatchRemoteState()
        }
    }

    override fun dispatch(entry: RemoteStateEntry, project: String?) {
        backgroundScope.launch {
            (project ?: currentProjectTagProvider.currentProject)?.let { project ->
                val dispatcher = dispatchers[project] ?: createProjectRemoteStateDispatcher(project)
                dispatcher.dispatch(entry)
            }
        }
    }

    private fun createProjectRemoteStateDispatcher(
        project: String,
        localStorage: LocalStorage,
        configuration: CoreConfiguration.RemoteState?
    ): ProjectRemoteStateDispatcher {
        configuration.let {
            localStorage.project.core.remoteState.configuration.value = it
        }
        return ProjectRemoteStateDispatcher(
            localStorage,
            project,
            coreAPI,
            signatureGenerator,
            configuration,
            PersistedQueue(appRemoteStateQueue),
            json,
            backgroundScope,
        )
    }

    private fun createPreviousProjectRemoteStateDispatcher(project: String): ProjectRemoteStateDispatcher {
        val localStorage = lazyLocalStorage.resolve()[project]
        val configuration = localStorage.project.core.remoteState.configuration.value
        val dispatcher = createProjectRemoteStateDispatcher(project, localStorage, configuration)
        dispatchers[project] = dispatcher
        return dispatcher
    }

    private suspend fun createCurrentProjectRemoteStateDispatcher() = coreConfigurationHolder.currentConfiguration
        .filterNotNull()
        .waitForContentApply(currentProjectTagProvider)
        .map { coreConfig ->
            createProjectRemoteStateDispatcher(
                currentProjectTagProvider.currentProject
                    ?: throw IllegalStateException("Project tag must be set at this step"),
                lazyLocalStorage.resolve(),
                coreConfig.remoteState
            ).apply {
                currentProjectTagProvider.currentProject?.let { dispatchers[it] = this }
            }
        }.first()

    @Suppress("NAME_SHADOWING")
    private suspend fun createProjectRemoteStateDispatcher(project: String?) =
        project?.let { project ->
            if (project == currentProjectTagProvider.currentProject) {
                createCurrentProjectRemoteStateDispatcher()
            } else {
                createPreviousProjectRemoteStateDispatcher(project)
            }
        } ?: createCurrentProjectRemoteStateDispatcher()
}
