package com.greencopper.core.services.iplocation

import kotlinx.coroutines.flow.Flow

public interface IPLocationService {
    /**
    This exists to assist `ServiceManager`, because it needs to know
    for certain when the `IPLocationService` has completed its task.

    A value of `false` means that the service hasn't run yet. A value
    of `true` means that it has. `true` does not imply success. The
    endpoint could time out or otherwise be unavailable. In that case,
    the `ServiceManager` should simply proceed. `true` means
    "IPLocationService has done what it can do. Move on." There is
    nothing `ServiceManager` can do to handle errors here.
     */
    public val completedFlow: Flow<Boolean>
}
