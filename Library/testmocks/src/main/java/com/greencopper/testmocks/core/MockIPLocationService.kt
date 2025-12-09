package com.greencopper.testmocks.core

import com.greencopper.core.services.iplocation.IPLocationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * The `completedFlow` is guaranteed to emit `true`,
 * and this somewhat dumb mock guarantees that.
 */
public class MockIPLocationService(
    override val completedFlow: Flow<Boolean> = flowOf(true)
): IPLocationService
