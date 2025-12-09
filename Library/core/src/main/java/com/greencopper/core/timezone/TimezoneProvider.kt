package com.greencopper.core.timezone

import java.time.ZoneId

public interface TimezoneProvider {

    public val zoneId: ZoneId
}