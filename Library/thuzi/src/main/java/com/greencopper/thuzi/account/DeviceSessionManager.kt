package com.greencopper.thuzi.account

import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.models.DeviceSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

public interface DeviceSessionManager {
    public fun getDeviceSession(project: String): DeviceSession
    public fun logout(project: String)
}

internal class ConcreteDeviceSessionManager(
    private val localStorage: LocalStorage,
    singleThreadScope: CoroutineScope,
    currentProjectTagProvider: CurrentProjectTagProvider,
) : DeviceSessionManager {

    init {
        singleThreadScope.launch {
            currentProjectTagProvider.currentProjectFlow
                .filterNotNull()
                .collect { project ->
                    // Set a new device session (if it doesn't exist) on project change, because
                    // the value may be accessed directly by LocalStorage::replaceUrlParameters
                    getDeviceSession(project)
                }
        }
    }

    @Synchronized
    override fun getDeviceSession(project: String): DeviceSession {
        val projectLocalStorage = localStorage[project].project

        return projectLocalStorage.thuzi.deviceSession.value ?:
            DeviceSession(installationId = localStorage.app.installationId.value).also {
                projectLocalStorage.thuzi.deviceSession.value = it
            }
    }

    override fun logout(project: String) {
        // A new DeviceSession is generated on logout, NOT on login. See the documentation for DeviceSession.
        localStorage[project].project.thuzi.deviceSession.value = DeviceSession(installationId = localStorage.app.installationId.value)
    }
}
