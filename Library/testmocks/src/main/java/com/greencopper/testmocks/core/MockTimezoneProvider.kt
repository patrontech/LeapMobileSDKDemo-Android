package com.greencopper.testmocks.core

import com.greencopper.core.timezone.TimezoneProvider
import java.time.ZoneId

public class MockTimezoneProvider(
    override val zoneId: ZoneId = ZoneId.systemDefault(),
) : TimezoneProvider
