package com.greencopper.thuzi.metrics

import com.greencopper.core.metrics.labels.EventName


public val EventName.Companion.fanScanCheckInSuccess: EventName
        by lazy { EventName("fan_scan/checkin_success") }

public val EventName.Companion.fanScanCheckInFailure: EventName
        by lazy { EventName("fan_scan/checkin_failure") }

public val EventName.Companion.thuziRegistration: EventName
        by lazy { EventName("thuzi_registration") }

public val EventName.Companion.deeplink: EventName
        by lazy { EventName("deeplink") }