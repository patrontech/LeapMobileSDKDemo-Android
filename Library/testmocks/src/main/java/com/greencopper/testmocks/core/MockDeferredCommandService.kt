package com.greencopper.testmocks.core

import com.greencopper.core.deferredcommand.DeferredCommandService
import com.greencopper.core.deferredcommand.DeferredCommandState

public class MockDeferredCommandService : DeferredCommandService {
    public val states: MutableSet<DeferredCommandState> = mutableSetOf()

    override suspend fun defer(state: DeferredCommandState) {
        states.add(state)
    }
}
