package com.greencopper.testmocks.toolkit

import com.greencopper.toolkit.net.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

public class MockNetworkMonitor(connected: Boolean = true) : NetworkMonitor {
    public val mutableConnectedFlow: MutableStateFlow<Boolean> = MutableStateFlow(connected)
    override val connectedFlow: Flow<Boolean> = mutableConnectedFlow
}
