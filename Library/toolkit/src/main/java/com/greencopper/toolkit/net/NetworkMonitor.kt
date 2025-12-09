package com.greencopper.toolkit.net

import kotlinx.coroutines.flow.Flow

public interface NetworkMonitor {
    public val connectedFlow: Flow<Boolean>
}