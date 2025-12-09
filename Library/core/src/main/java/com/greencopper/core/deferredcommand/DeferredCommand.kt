package com.greencopper.core.deferredcommand

public interface DeferredCommand {
    public val key: DeferredCommandKey
    public suspend fun execute(state: DeferredCommandState): Set<DeferredCommandState>
}