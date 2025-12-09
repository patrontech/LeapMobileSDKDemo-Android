package com.greencopper.core.remotestate.models

import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.core.remotestate.dispatch
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.serialization.json.JsonPrimitive

/**
 * CMS remote dispatch data, to be sent on App installs or updates only.
 * Should not be sent on each app launch.
 */
public data class AppRemoteStateEntry(
    private val appVersion: String,
    private val buildConfig: BuildConfigProvider
) {
    public fun dispatch(remoteStateDispatcher: RemoteStateDispatcher) {
        with(remoteStateDispatcher) {
            dispatch("platform", JsonPrimitive("android"), RemoteStateEntry.Domain.APP, false)
            dispatch("app_version", JsonPrimitive(appVersion), RemoteStateEntry.Domain.APP,false)
            dispatch("device", JsonPrimitive("${buildConfig.manufacturer} ${buildConfig.model}"), RemoteStateEntry.Domain.APP,false)
            dispatch("os_version", JsonPrimitive("${buildConfig.sdkInt}"), RemoteStateEntry.Domain.APP,false)
        }
    }
}
