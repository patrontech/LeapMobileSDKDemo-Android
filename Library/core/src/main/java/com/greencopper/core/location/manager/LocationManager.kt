package com.greencopper.core.location.manager

import com.greencopper.core.location.recipe.Region

public interface LocationManager {
    public val monitoredRegions: Set<Region>
    public suspend fun updateState(regionId: Int, entered: Boolean)
    public suspend fun startMonitoring(region: Region)
    public fun stopMonitoring(region: Region)
    public suspend fun resetMonitoring(regions: List<Region>)
}
