package com.greencopper.testmocks

import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import kotlinx.serialization.json.Json

public class MockRemoteStateDispatcher(
    override val json: Json
): RemoteStateDispatcher {
    public var dispatchedEntry: RemoteStateEntry? = null
    public var dispatchCallCount: Int = 0
    public var dispatchProject: String? = null

    override fun dispatch(entry: RemoteStateEntry, project: String?) {
        dispatchedEntry = entry
        dispatchCallCount++
        dispatchProject = project
    }
}
